package com.lifelinkai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmbulanceLocationResponse {
    private String ambulanceId;
    private String emergencyId;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Double etaMinutes;
    private Boolean simulated;
    private LocalDateTime timestamp;
}
