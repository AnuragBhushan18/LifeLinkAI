package com.lifelinkai.backend.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelinkai.backend.model.BedStatus;
import com.lifelinkai.backend.model.BedType;
import com.lifelinkai.backend.repository.BedRepository;
import com.lifelinkai.backend.repository.EmergencyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIToolRegistry {
    private final EmergencyRequestRepository emergencyRepository;
    private final BedRepository bedRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getToolsJson() {
        return "[{\"name\": \"getEmergencyCount\", \"description\": \"Gets total number of emergencies\"}," +
               " {\"name\": \"getAvailableICUBeds\", \"description\": \"Gets count of available ICU beds across all hospitals\"}," +
               " {\"name\": \"getAvailableBeds\", \"description\": \"Gets count of available beds of all types\"}]";
    }

    public String executeTool(String toolName, Map<String, Object> args) {
        try {
            switch (toolName) {
                case "getEmergencyCount":
                    long count = emergencyRepository.count();
                    return "Total emergencies: " + count;
                case "getAvailableICUBeds":
                    // Not ideal to search all hospitals without hospitalId, but this is a global query for admin.
                    // For simplicity, we just count all beds globally that are ICU and AVAILABLE
                    // Wait, Spring Data MongoDB: countByTypeAndStatus is not globally defined in BedRepository? Let's check.
                    long icuCount = bedRepository.findAll().stream().filter(b -> b.getType() == BedType.ICU && b.getStatus() == BedStatus.AVAILABLE).count();
                    return "Available ICU beds: " + icuCount;
                case "getAvailableBeds":
                    long availableCount = bedRepository.findAll().stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
                    return "Available beds: " + availableCount;
                default:
                    return "Tool not found or unauthorized.";
            }
        } catch (Exception e) {
            return "Error executing tool: " + e.getMessage();
        }
    }
}