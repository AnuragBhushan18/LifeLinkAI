package com.lifelinkai.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medical_records")
public class MedicalRecord {
    @Id
    private String id;
    
    @Indexed
    private String patientId;
    
    @Indexed
    private String hospitalId;
    
    @Indexed
    private String doctorId;
    
    @Indexed
    private String emergencyId;
    
    private BloodGroup bloodGroup;
    private List<String> allergies;
    private List<String> currentMedications;
    private String medicalHistory;
    private String clinicalNotes;
    private String treatmentNotes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
