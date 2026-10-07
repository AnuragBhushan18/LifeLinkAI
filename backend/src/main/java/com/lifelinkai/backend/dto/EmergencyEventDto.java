package com.lifelinkai.backend.dto;

import com.lifelinkai.backend.model.EmergencySeverity;
import com.lifelinkai.backend.model.EmergencyStatus;
import com.lifelinkai.backend.model.EmergencyTimelineEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyEventDto {
    private String eventType;
    private String emergencyId;
    private EmergencyStatus status;
    private EmergencySeverity severity;
    private LocalDateTime timestamp;
    private String message;
    private Double latitude;
    private Double longitude;
    private Double etaMinutes;
    private Double distanceKm;
    private String ambulanceId;
    private String driverId;
    private String hospitalId;
    private List<EmergencyTimelineEvent> timeline;
}
