package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.AmbulanceAllocationResponse;
import com.lifelinkai.backend.model.Ambulance;
import com.lifelinkai.backend.model.AmbulanceStatus;
import com.lifelinkai.backend.model.AmbulanceType;
import com.lifelinkai.backend.model.EmergencySeverity;
import com.lifelinkai.backend.repository.AmbulanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmbulanceAllocationService {

    private final AmbulanceRepository ambulanceRepository;
    private final DistanceService distanceService;

    public AmbulanceAllocationResponse allocateAmbulance(double lat, double lon, EmergencySeverity severity) {
        List<Ambulance> ambulances = ambulanceRepository.findAll();
        
        Ambulance bestAmbulance = null;
        double bestScore = -1;
        double bestDistance = 0;
        double bestEta = 0;
        List<String> bestReasons = new ArrayList<>();
        
        for (Ambulance ambulance : ambulances) {
            if (ambulance.getStatus() != AmbulanceStatus.IDLE || 
                ambulance.getLatitude() == null || ambulance.getLongitude() == null) {
                continue;
            }
            
            if (!isTypeSuitable(ambulance.getType(), severity)) {
                continue;
            }
            
            double distanceKm = distanceService.calculateDistanceKm(lat, lon, ambulance.getLatitude(), ambulance.getLongitude());
            double etaMinutes = distanceService.estimateEtaMinutes(distanceKm);
            
            // Score based on distance (closer is better)
            double distanceScore = Math.max(0, (30.0 - distanceKm) / 30.0) * 100;
            
            // Prefer advanced for higher severity
            double typeScore = 50;
            if (severity == EmergencySeverity.CRITICAL && ambulance.getType() == AmbulanceType.ICU) typeScore = 100;
            if (severity == EmergencySeverity.HIGH && ambulance.getType() == AmbulanceType.ADVANCED) typeScore = 100;
            
            double totalScore = (distanceScore * 0.7) + (typeScore * 0.3);
            
            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestAmbulance = ambulance;
                bestDistance = distanceKm;
                bestEta = etaMinutes;
                
                bestReasons.clear();
                bestReasons.add("Score: " + Math.round(totalScore));
                if (distanceKm < 5) bestReasons.add("Very close to patient");
                bestReasons.add("Suitable ambulance type: " + ambulance.getType());
            }
        }
        
        if (bestAmbulance == null) {
            return null; // No suitable ambulance
        }
        
        return AmbulanceAllocationResponse.builder()
                .ambulanceId(bestAmbulance.getId())
                .driverId(bestAmbulance.getDriverId())
                .vehicleNumber(bestAmbulance.getVehicleNumber())
                .score(Math.round(bestScore * 10.0) / 10.0)
                .distanceKm(Math.round(bestDistance * 10.0) / 10.0)
                .estimatedEtaMinutes(Math.round(bestEta))
                .reasons(bestReasons)
                .build();
    }
    
    private boolean isTypeSuitable(AmbulanceType type, EmergencySeverity severity) {
        if (type == null) return true; // fallback
        
        switch (severity) {
            case CRITICAL:
                return type == AmbulanceType.ICU || type == AmbulanceType.ADVANCED;
            case HIGH:
                return type == AmbulanceType.ADVANCED || type == AmbulanceType.ICU;
            case MEDIUM:
                return true; // Any type is fine
            case LOW:
                return true;
            default:
                return true;
        }
    }
}
