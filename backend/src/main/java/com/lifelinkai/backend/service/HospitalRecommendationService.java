package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.HospitalRecommendationResponse;
import com.lifelinkai.backend.model.Hospital;
import com.lifelinkai.backend.model.OperationalStatus;
import com.lifelinkai.backend.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalRecommendationService {

    private final HospitalRepository hospitalRepository;
    private final DistanceService distanceService;

    // Weights
    private static final double DISTANCE_WEIGHT = 0.4;
    private static final double BEDS_WEIGHT = 0.2;
    private static final double ICU_WEIGHT = 0.3;
    private static final double OPERATIONAL_WEIGHT = 0.1;

    public HospitalRecommendationResponse recommendHospital(double lat, double lon) {
        List<Hospital> hospitals = hospitalRepository.findAll();
        
        if (hospitals.isEmpty()) {
            return null;
        }
        
        Hospital bestHospital = null;
        double bestScore = -1.0;
        double bestDistance = 0;
        double bestEta = 0;
        List<String> bestReasons = new ArrayList<>();
        
        for (Hospital hospital : hospitals) {
            if (hospital.getOperationalStatus() == OperationalStatus.INACTIVE || 
                hospital.getLatitude() == null || hospital.getLongitude() == null) {
                continue;
            }
            
            double distanceKm = distanceService.calculateDistanceKm(lat, lon, hospital.getLatitude(), hospital.getLongitude());
            double etaMinutes = distanceService.estimateEtaMinutes(distanceKm);
            
            // Normalize scores (example logic)
            // Distance score: closer is better, max distance considered ~50km for scoring purposes
            double distanceScore = Math.max(0, (50.0 - distanceKm) / 50.0) * 100;
            
            int availBeds = hospital.getAvailableBeds() != null ? hospital.getAvailableBeds() : 0;
            int availIcu = hospital.getAvailableIcuBeds() != null ? hospital.getAvailableIcuBeds() : 0;
            
            // Beds score
            double bedScore = Math.min(100, (availBeds / 50.0) * 100);
            
            // ICU score
            double icuScore = Math.min(100, (availIcu / 10.0) * 100);
            
            // Status score
            double statusScore = hospital.getOperationalStatus() == OperationalStatus.ACTIVE ? 100 : 50;
            
            double totalScore = (distanceScore * DISTANCE_WEIGHT) +
                                (bedScore * BEDS_WEIGHT) +
                                (icuScore * ICU_WEIGHT) +
                                (statusScore * OPERATIONAL_WEIGHT);
                                
            if (totalScore > bestScore && availBeds > 0) {
                bestScore = totalScore;
                bestHospital = hospital;
                bestDistance = distanceKm;
                bestEta = etaMinutes;
                
                bestReasons.clear();
                bestReasons.add("Score: " + Math.round(totalScore));
                if (distanceKm < 10) bestReasons.add("Low travel distance");
                if (availIcu > 0) bestReasons.add("ICU available");
                if (hospital.getOperationalStatus() == OperationalStatus.ACTIVE) bestReasons.add("Hospital operational status is ACTIVE");
            }
        }
        
        if (bestHospital == null) {
            return null; // No suitable hospital found
        }
        
        return HospitalRecommendationResponse.builder()
                .hospitalId(bestHospital.getId())
                .hospitalName(bestHospital.getName())
                .score(Math.round(bestScore * 10.0) / 10.0)
                .distanceKm(Math.round(bestDistance * 10.0) / 10.0)
                .estimatedEtaMinutes(Math.round(bestEta))
                .availableBeds(bestHospital.getAvailableBeds())
                .availableIcuBeds(bestHospital.getAvailableIcuBeds())
                .specialistAvailable(true) // Simplified for Phase 3
                .reasons(bestReasons)
                .build();
    }
}
