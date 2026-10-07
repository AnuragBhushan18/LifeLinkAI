package com.lifelinkai.backend.websocket;

import com.lifelinkai.backend.dto.AmbulanceLocationResponse;
import com.lifelinkai.backend.dto.EmergencyEventDto;
import com.lifelinkai.backend.dto.NotificationDto;
import com.lifelinkai.backend.model.EmergencyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final com.lifelinkai.backend.repository.DriverRepository driverRepository;
    private final com.lifelinkai.backend.repository.HospitalRepository hospitalRepository;

    public void publishEmergencyEvent(EmergencyEventDto event, EmergencyRequest emergency) {
        log.info("Broadcasting emergency event: {} for emergencyId: {}", event.getEventType(), event.getEmergencyId());

        // 1. Topic for the specific emergency
        messagingTemplate.convertAndSend("/topic/emergency/" + emergency.getId(), event);

        // 2. Patient topic
        if (emergency.getPatientId() != null) {
            messagingTemplate.convertAndSend("/topic/patient/" + emergency.getPatientId(), event);
        }

        // 3. Driver topic (broadcast to both driver entity ID and driver user ID)
        if (emergency.getAssignedDriverId() != null) {
            messagingTemplate.convertAndSend("/topic/driver/" + emergency.getAssignedDriverId(), event);
            driverRepository.findById(emergency.getAssignedDriverId()).ifPresent(d -> {
                if (d.getUserId() != null && !d.getUserId().equals(emergency.getAssignedDriverId())) {
                    messagingTemplate.convertAndSend("/topic/driver/" + d.getUserId(), event);
                }
            });
        }

        // 4. Hospital topic (broadcast to both hospital entity ID and hospital user ID)
        if (emergency.getRecommendedHospitalId() != null) {
            messagingTemplate.convertAndSend("/topic/hospital/" + emergency.getRecommendedHospitalId(), event);
            hospitalRepository.findById(emergency.getRecommendedHospitalId()).ifPresent(h -> {
                if (h.getUserId() != null && !h.getUserId().equals(emergency.getRecommendedHospitalId())) {
                    messagingTemplate.convertAndSend("/topic/hospital/" + h.getUserId(), event);
                }
            });
        }

        // 5. Admin topic
        messagingTemplate.convertAndSend("/topic/admin/emergencies", event);
    }

    public void publishLocationUpdate(AmbulanceLocationResponse location, EmergencyRequest emergency) {
        log.debug("Broadcasting location update for emergencyId: {}, lat: {}, lon: {}", 
                location.getEmergencyId(), location.getLatitude(), location.getLongitude());

        EmergencyEventDto event = EmergencyEventDto.builder()
                .eventType("LOCATION_UPDATED")
                .emergencyId(location.getEmergencyId())
                .status(emergency != null ? emergency.getStatus() : null)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .etaMinutes(location.getEtaMinutes())
                .distanceKm(location.getDistanceKm())
                .ambulanceId(location.getAmbulanceId())
                .timestamp(location.getTimestamp())
                .message(Boolean.TRUE.equals(location.getSimulated()) 
                        ? "Demo / Simulated Location update" 
                        : "Ambulance location updated")
                .build();

        messagingTemplate.convertAndSend("/topic/emergency/" + location.getEmergencyId(), event);
    }

    public void publishNotification(String recipientId, NotificationDto notification) {
        log.info("Publishing notification to user: {} (type: {})", recipientId, notification.getType());
        messagingTemplate.convertAndSend("/topic/user/" + recipientId + "/notifications", notification);
    }
}
