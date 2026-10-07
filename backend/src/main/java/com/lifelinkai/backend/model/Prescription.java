package com.lifelinkai.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prescriptions")
public class Prescription {
    @Id
    private String id;
    
    @Indexed
    private String patientId;
    
    @Indexed
    private String doctorId;
    
    @Indexed
    private String hospitalId;
    
    @Indexed
    private String emergencyId;
    
    private List<MedicineItem> medicines;
    private String instructions;
    
    private LocalDateTime prescribedAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
