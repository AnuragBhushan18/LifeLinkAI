package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.EmergencyCreateRequest;
import com.lifelinkai.backend.model.EmergencyRequest;
import com.lifelinkai.backend.model.EmergencyStatus;
import com.lifelinkai.backend.model.Role;
import com.lifelinkai.backend.model.User;
import com.lifelinkai.backend.security.CustomUserDetails;
import com.lifelinkai.backend.service.EmergencyService;
import com.lifelinkai.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;
    private final UserRepository userRepository;

    private final com.lifelinkai.backend.repository.DriverRepository driverRepository;
    private final com.lifelinkai.backend.repository.HospitalRepository hospitalRepository;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<EmergencyRequest> createEmergency(
            @RequestBody EmergencyCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        EmergencyRequest emergency = emergencyService.createEmergency(user.getId(), request);
        return ResponseEntity.ok(emergency);
    }
    
    @GetMapping
    public ResponseEntity<List<EmergencyRequest>> getEmergencies(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (user.getRole() == Role.PATIENT) {
            return ResponseEntity.ok(emergencyService.getPatientEmergencies(user.getId()));
        } else if (user.getRole() == Role.AMBULANCE_DRIVER) {
            return ResponseEntity.ok(emergencyService.getDriverEmergencies(user.getId()));
        } else if (user.getRole() == Role.HOSPITAL_STAFF) {
            return ResponseEntity.ok(emergencyService.getHospitalEmergencies(user.getId()));
        } else if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(emergencyService.getAllEmergencies());
        }
        
        return ResponseEntity.status(403).build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmergencyRequest> getEmergency(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        EmergencyRequest emergency = emergencyService.getEmergency(id);
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (user.getRole() == Role.PATIENT && !user.getId().equals(emergency.getPatientId())) {
            return ResponseEntity.status(403).build();
        } else if (user.getRole() == Role.AMBULANCE_DRIVER) {
            boolean matches = user.getId().equals(emergency.getAssignedDriverId()) ||
                    driverRepository.findByUserId(user.getId())
                            .map(d -> d.getId().equals(emergency.getAssignedDriverId()))
                            .orElse(false);
            if (!matches) return ResponseEntity.status(403).build();
        } else if (user.getRole() == Role.HOSPITAL_STAFF) {
            boolean matches = user.getId().equals(emergency.getRecommendedHospitalId()) ||
                    hospitalRepository.findByUserId(user.getId())
                            .map(h -> h.getId().equals(emergency.getRecommendedHospitalId()))
                            .orElse(false);
            if (!matches) return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(emergency);
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<com.lifelinkai.backend.model.EmergencyTimelineEvent>> getTimeline(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EmergencyRequest emergency = emergencyService.getEmergency(id);
        return ResponseEntity.ok(emergency.getTimeline() != null ? emergency.getTimeline() : List.of());
    }
    
    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<EmergencyRequest> processEmergency(@PathVariable String id) {
        EmergencyRequest emergency = emergencyService.processEmergency(id);
        return ResponseEntity.ok(emergency);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<EmergencyRequest> updateStatus(
            @PathVariable String id,
            @RequestParam EmergencyStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        EmergencyRequest emergency = emergencyService.getEmergency(id);

        if (user.getRole() == Role.PATIENT) {
            if (!user.getId().equals(emergency.getPatientId()) || status != EmergencyStatus.CANCELLED) {
                return ResponseEntity.status(403).build();
            }
        } else if (user.getRole() == Role.AMBULANCE_DRIVER) {
            boolean matches = user.getId().equals(emergency.getAssignedDriverId()) ||
                    driverRepository.findByUserId(user.getId())
                            .map(d -> d.getId().equals(emergency.getAssignedDriverId()))
                            .orElse(false);
            if (!matches) return ResponseEntity.status(403).build();
        } else if (user.getRole() == Role.HOSPITAL_STAFF) {
            boolean matches = user.getId().equals(emergency.getRecommendedHospitalId()) ||
                    hospitalRepository.findByUserId(user.getId())
                            .map(h -> h.getId().equals(emergency.getRecommendedHospitalId()))
                            .orElse(false);
            if (!matches) return ResponseEntity.status(403).build();
        }

        EmergencyRequest updated = emergencyService.updateEmergencyStatus(id, status);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<EmergencyRequest> cancelEmergency(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        EmergencyRequest emergency = emergencyService.getEmergency(id);
        if (!emergency.getPatientId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        emergency = emergencyService.updateEmergencyStatus(id, EmergencyStatus.CANCELLED);
        return ResponseEntity.ok(emergency);
    }
}
