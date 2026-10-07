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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medical_reports")
public class MedicalReport {
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
    
    private String title;
    private String summary;
    private String diagnosisNotes;
    private String treatmentProvided;
    private String recommendations;
    
    private LocalDateTime reportDate;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
