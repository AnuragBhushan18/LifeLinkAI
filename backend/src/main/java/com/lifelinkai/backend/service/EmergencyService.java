package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.AmbulanceAllocationResponse;
import com.lifelinkai.backend.dto.EmergencyCreateRequest;
import com.lifelinkai.backend.dto.EmergencySeverityResult;
import com.lifelinkai.backend.dto.HospitalRecommendationResponse;
import com.lifelinkai.backend.model.Ambulance;
import com.lifelinkai.backend.model.AmbulanceStatus;
import com.lifelinkai.backend.model.EmergencyRequest;
import com.lifelinkai.backend.model.EmergencyStatus;
import com.lifelinkai.backend.repository.AmbulanceRepository;
import com.lifelinkai.backend.repository.EmergencyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyRequestRepository emergencyRepository;
    private final EmergencySeverityService severityService;
    private final HospitalRecommendationService hospitalService;
    private final AmbulanceAllocationService ambulanceService;
    private final EmergencyStateService stateService;
    private final AmbulanceRepository ambulanceRepository;


    public EmergencyRequest createEmergency(String patientId, EmergencyCreateRequest request) {
        EmergencyRequest emergency = EmergencyRequest.builder()
                .patientId(patientId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .symptoms(request.getSymptoms())
                .emergencyDescription(request.getEmergencyDescription())
                .status(EmergencyStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
        emergency = emergencyRepository.save(emergency);
        
        // Start background processing or synchronous processing for Phase 3
        return processEmergency(emergency.getId());
    }
    

    public EmergencyRequest processEmergency(String id) {
        EmergencyRequest emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency not found"));
                
        // 1. Severity Analysis
        if (emergency.getStatus() == EmergencyStatus.CREATED) {
            EmergencySeverityResult severityResult = severityService.analyzeSeverity(
                    emergency.getSymptoms(), emergency.getEmergencyDescription());
                    
            emergency.setSeverity(severityResult.getSeverity());
            emergency.setSeverityFactors(severityResult.getSeverityFactors());
            
            updateState(emergency, EmergencyStatus.SEVERITY_ANALYZED);
        }
        
        // 2. Hospital Recommendation
        if (emergency.getStatus() == EmergencyStatus.SEVERITY_ANALYZED) {
            HospitalRecommendationResponse hospitalRes = hospitalService.recommendHospital(
                    emergency.getLatitude(), emergency.getLongitude());
                    
            if (hospitalRes != null) {
                emergency.setRecommendedHospitalId(hospitalRes.getHospitalId());
                updateState(emergency, EmergencyStatus.AMBULANCE_SEARCHING);
            } else {
                // If no hospital found, keep in analyzed state or mark for manual review
                // For now, keep it in analyzed state
            }
        }
        
        // 3. Ambulance Allocation
        if (emergency.getStatus() == EmergencyStatus.AMBULANCE_SEARCHING) {
            AmbulanceAllocationResponse ambulanceRes = ambulanceService.allocateAmbulance(
                    emergency.getLatitude(), emergency.getLongitude(), emergency.getSeverity());
                    
            if (ambulanceRes != null) {
                // Assign ambulance
                emergency.setAssignedAmbulanceId(ambulanceRes.getAmbulanceId());
                emergency.setAssignedDriverId(ambulanceRes.getDriverId());
                emergency.setAmbulanceAssignedAt(LocalDateTime.now());
                
                // Update Ambulance state
                Ambulance ambulance = ambulanceRepository.findById(ambulanceRes.getAmbulanceId()).orElse(null);
                if (ambulance != null) {
                    ambulance.setStatus(AmbulanceStatus.ASSIGNED); // assuming ASSIGNED exists or use IN_TRANSIT
                    ambulanceRepository.save(ambulance);
                }
                
                updateState(emergency, EmergencyStatus.AMBULANCE_ASSIGNED);
            }
        }
        
        return emergencyRepository.save(emergency);
    }
    
    public EmergencyRequest updateEmergencyStatus(String id, EmergencyStatus newStatus) {
        EmergencyRequest emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency not found"));
                
        if (!stateService.isValidTransition(emergency.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid state transition from " + emergency.getStatus() + " to " + newStatus);
        }
        
        emergency.setStatus(newStatus);
        emergency.setUpdatedAt(LocalDateTime.now());
        
        if (newStatus == EmergencyStatus.COMPLETED) {
            emergency.setCompletedAt(LocalDateTime.now());
        }
        
        return emergencyRepository.save(emergency);
    }
    
    private void updateState(EmergencyRequest emergency, EmergencyStatus newStatus) {
        if (stateService.isValidTransition(emergency.getStatus(), newStatus)) {
            emergency.setStatus(newStatus);
            emergency.setUpdatedAt(LocalDateTime.now());
        }
    }
    
    public EmergencyRequest getEmergency(String id) {
        return emergencyRepository.findById(id).orElseThrow(() -> new RuntimeException("Emergency not found"));
    }
    
    public List<EmergencyRequest> getAllEmergencies() {
        return emergencyRepository.findAll();
    }
    
    public List<EmergencyRequest> getPatientEmergencies(String patientId) {
        return emergencyRepository.findByPatientId(patientId);
    }
    
    public List<EmergencyRequest> getHospitalEmergencies(String hospitalId) {
        return emergencyRepository.findByRecommendedHospitalId(hospitalId);
    }
    
    public List<EmergencyRequest> getDriverEmergencies(String driverId) {
        return emergencyRepository.findByAssignedDriverId(driverId);
    }
}
