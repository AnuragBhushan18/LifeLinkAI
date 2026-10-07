package com.lifelinkai.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AmbulanceAllocationResponse {
    private String ambulanceId;
    private String driverId;
    private String vehicleNumber;
    private double score;
    private double distanceKm;
    private double estimatedEtaMinutes;
    private List<String> reasons;
}
