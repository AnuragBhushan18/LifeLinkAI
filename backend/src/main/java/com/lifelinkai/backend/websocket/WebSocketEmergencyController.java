package com.lifelinkai.backend.websocket;

import com.lifelinkai.backend.dto.AmbulanceLocationRequest;
import com.lifelinkai.backend.dto.AmbulanceLocationResponse;
import com.lifelinkai.backend.model.EmergencyRequest;
import com.lifelinkai.backend.model.EmergencyStatus;
import com.lifelinkai.backend.service.AmbulanceLocationService;
import com.lifelinkai.backend.service.EmergencyService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketEmergencyController {

    private final AmbulanceLocationService ambulanceLocationService;
    private final EmergencyService emergencyService;

    @MessageMapping("/ambulance/location")
    public void handleLocationUpdate(@Payload AmbulanceLocationRequest request, Principal principal) {
        if (principal == null) {
            log.warn("Rejected location update from unauthenticated WebSocket principal");
            return;
        }
        log.debug("Received WebSocket location update from {}: lat={}, lon={}", 
                principal.getName(), request.getLatitude(), request.getLongitude());
        ambulanceLocationService.updateDriverLocation(principal.getName(), request);
    }

    @MessageMapping("/emergency/status")
    public void handleStatusUpdate(@Payload EmergencyStatusUpdateMessage message, Principal principal) {
        if (principal == null) {
            log.warn("Rejected status update from unauthenticated WebSocket principal");
            return;
        }
        log.info("Received WebSocket status update from {}: emergencyId={}, status={}", 
                principal.getName(), message.getEmergencyId(), message.getStatus());
        emergencyService.updateEmergencyStatus(message.getEmergencyId(), message.getStatus());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyStatusUpdateMessage {
        private String emergencyId;
        private EmergencyStatus status;
    }
}
