package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.EmergencySeverityResult;
import com.lifelinkai.backend.model.EmergencySeverity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class EmergencySeverityService {

    // Configurable scores for symptoms
    private static final Map<String, Integer> SYMPTOM_SCORES = new HashMap<>();
    static {
        SYMPTOM_SCORES.put("difficulty breathing", 40);
        SYMPTOM_SCORES.put("chest pain", 40);
        SYMPTOM_SCORES.put("unconsciousness", 50);
        SYMPTOM_SCORES.put("severe bleeding", 40);
        SYMPTOM_SCORES.put("seizure", 30);
        SYMPTOM_SCORES.put("stroke-like symptoms", 40);
        SYMPTOM_SCORES.put("major accident", 50);
        SYMPTOM_SCORES.put("severe burns", 40);
        
        SYMPTOM_SCORES.put("fever", 5);
        SYMPTOM_SCORES.put("mild pain", 5);
        SYMPTOM_SCORES.put("nausea", 5);
        SYMPTOM_SCORES.put("dizziness", 10);
    }

    public EmergencySeverityResult analyzeSeverity(List<String> symptoms, String description) {
        int totalScore = 0;
        List<String> factors = new ArrayList<>();
        
        if (symptoms != null) {
            for (String symptom : symptoms) {
                String s = symptom.toLowerCase().trim();
                for (Map.Entry<String, Integer> entry : SYMPTOM_SCORES.entrySet()) {
                    if (s.contains(entry.getKey())) {
                        totalScore += entry.getValue();
                        factors.add(entry.getKey());
                    }
                }
            }
        }
        
        if (description != null) {
            String d = description.toLowerCase();
            for (Map.Entry<String, Integer> entry : SYMPTOM_SCORES.entrySet()) {
                if (d.contains(entry.getKey()) && !factors.contains(entry.getKey())) {
                    totalScore += entry.getValue();
                    factors.add("description mentions " + entry.getKey());
                }
            }
        }
        
        EmergencySeverity severity = determineSeverityFromScore(totalScore);
        
        if (factors.isEmpty()) {
            factors.add("No critical symptoms identified");
        }
        
        return EmergencySeverityResult.builder()
                .severity(severity)
                .score(totalScore)
                .severityFactors(factors)
                .build();
    }

    private EmergencySeverity determineSeverityFromScore(int score) {
        if (score >= 60) return EmergencySeverity.CRITICAL;
        if (score >= 30) return EmergencySeverity.HIGH;
        if (score >= 10) return EmergencySeverity.MEDIUM;
        return EmergencySeverity.LOW;
    }
}
