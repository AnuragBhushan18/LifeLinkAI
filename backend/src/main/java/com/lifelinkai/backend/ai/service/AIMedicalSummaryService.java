package com.lifelinkai.backend.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelinkai.backend.dto.ai.MedicalSummaryResponse;
import com.lifelinkai.backend.model.AIMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIMedicalSummaryService {
    private final LLMService llmService;
    private final PromptService promptService;
    private final AIContextService contextService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MedicalSummaryResponse generateSummary(String patientId) {
        String patientDataJson = contextService.buildPatientContext(patientId);

        AIMessage userMsg = new AIMessage("user", "Summarize this patient's relevant medical history: " + patientDataJson, LocalDateTime.now());

        String jsonResponse = llmService.generateResponse(List.of(userMsg), promptService.getDoctorSummarizerPrompt());

        try {
            // Remove markdown formatting if LLM includes it
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
            } else if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
            }

            MedicalSummaryResponse response = objectMapper.readValue(jsonResponse, MedicalSummaryResponse.class);
            response.setPatientId(patientId);
            response.setGeneratedAt(LocalDateTime.now());
            response.setModel(llmService.getActiveProvider().getModelName());
            response.setDisclaimer("AI-generated summary \u2014 verify against original medical records.");
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM structured response: " + e.getMessage());
        }
    }
}