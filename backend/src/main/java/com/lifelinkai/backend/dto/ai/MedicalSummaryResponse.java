package com.lifelinkai.backend.dto.ai;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalSummaryResponse {
    private String patientId;
    private String summary;
    private List<String> allergies;
    private List<String> medications;
    private String relevantHistory;
    private String recentReports;
    private String importantNotes;
    private List<String> missingInformation;
    private LocalDateTime generatedAt;
    private String model;
    private String disclaimer;
}