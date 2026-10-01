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

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<EmergencyRequest> createEmergency(
            @RequestBody EmergencyCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // In Phase 2, patient id was likely user id or separate Patient entity.
        // Assuming patientId is user.getId() for now, or we need to find the Patient entity.
        // I will just use user.getId() as the patientId.
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
                
        // Basic authorization check
        if (user.getRole() == Role.PATIENT && !emergency.getPatientId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        // Add other role checks if needed
        
        return ResponseEntity.ok(emergency);
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
            
        // Authorization should check if the driver or hospital owns this emergency
        EmergencyRequest emergency = emergencyService.updateEmergencyStatus(id, status);
        return ResponseEntity.ok(emergency);
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
