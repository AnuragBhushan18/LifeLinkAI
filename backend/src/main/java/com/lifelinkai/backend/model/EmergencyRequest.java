package com.lifelinkai.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "emergency_requests")
public class EmergencyRequest {
    @Id
    private String id;
    
    @Indexed
    private String patientId;
    
    private double latitude;
    private double longitude;
    
    private List<String> symptoms;
    private String emergencyDescription;
    
    @Indexed
    private EmergencySeverity severity;
    private List<String> severityFactors;
    
    @Indexed
    private String recommendedHospitalId;
    
    @Indexed
    private String assignedAmbulanceId;
    
    @Indexed
    private String assignedDriverId;
    
    @Indexed
    private EmergencyStatus status;
    
    @Indexed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private LocalDateTime hospitalAcceptedAt;
    private LocalDateTime ambulanceAssignedAt;
    private LocalDateTime completedAt;
}
