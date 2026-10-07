package com.lifelinkai.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HospitalRecommendationResponse {
    private String hospitalId;
    private String hospitalName;
    private double score;
    private double distanceKm;
    private double estimatedEtaMinutes;
    private int availableBeds;
    private int availableIcuBeds;
    private boolean specialistAvailable;
    private List<String> reasons;
}
