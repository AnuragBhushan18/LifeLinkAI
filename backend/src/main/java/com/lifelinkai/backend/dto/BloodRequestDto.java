package com.lifelinkai.backend.dto;
import lombok.Data;
import com.lifelinkai.backend.model.BloodGroup;
import com.lifelinkai.backend.model.RequestPriority;
import com.lifelinkai.backend.model.BloodRequestStatus;
import java.time.LocalDateTime;
@Data
public class BloodRequestDto {
    private String id;
    private String hospitalId;
    private String bloodBankId;
    private String emergencyId;
    private String requestedBy;
    private BloodGroup bloodGroup;
    private int unitsRequested;
    private RequestPriority priority;
    private BloodRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}