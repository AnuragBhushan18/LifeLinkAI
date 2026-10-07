package com.lifelinkai.backend.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class MedicalReportDto {
    private String id;
    private String patientId;
    private String hospitalId;
    private String doctorId;
    private String emergencyId;
    private String title;
    private String summary;
    private String diagnosisNotes;
    private String treatmentProvided;
    private String recommendations;
    private LocalDateTime reportDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}