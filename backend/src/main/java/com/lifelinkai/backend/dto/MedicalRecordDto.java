package com.lifelinkai.backend.dto;
import lombok.Data;
import com.lifelinkai.backend.model.BloodGroup;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class MedicalRecordDto {
    private String id;
    private String patientId;
    private String hospitalId;
    private String doctorId;
    private String emergencyId;
    private BloodGroup bloodGroup;
    private List<String> allergies;
    private List<String> currentMedications;
    private String medicalHistory;
    private String clinicalNotes;
    private String treatmentNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}