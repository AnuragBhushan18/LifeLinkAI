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
@Document(collection = "blood_requests")
public class BloodRequest {
    @Id
    private String id;
    
    @Indexed
    private String hospitalId;
    
    @Indexed
    private String bloodBankId;
    
    @Indexed
    private String emergencyId;
    
    private String requestedBy;
    
    private BloodGroup bloodGroup;
    private int unitsRequested;
    
    private RequestPriority priority;
    
    @Indexed
    private BloodRequestStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
