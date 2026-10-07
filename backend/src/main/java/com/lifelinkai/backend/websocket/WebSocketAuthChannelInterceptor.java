package com.lifelinkai.backend.websocket;

import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.AmbulanceRepository;
import com.lifelinkai.backend.repository.DriverRepository;
import com.lifelinkai.backend.repository.EmergencyRequestRepository;
import com.lifelinkai.backend.repository.HospitalRepository;
import com.lifelinkai.backend.repository.UserRepository;
import com.lifelinkai.backend.security.CustomUserDetails;
import com.lifelinkai.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final EmergencyRequestRepository emergencyRepository;
    private final DriverRepository driverRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final com.lifelinkai.backend.repository.PatientRepository patientRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                if (!accessor.isMutable()) {
                    StompHeaderAccessor mutableAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
                    mutableAccessor.copyHeaders(message.getHeaders());
                    mutableAccessor.setLeaveMutable(true);
                    accessor = mutableAccessor;
                }
                authenticateConnection(accessor);
                return org.springframework.messaging.support.MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                authorizeSubscription(accessor);
            } else if (StompCommand.SEND.equals(accessor.getCommand())) {
                authorizeSend(accessor);
            }
        }

        return message;
    }

    private void authenticateConnection(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);

        if (token == null || token.isBlank()) {
            log.warn("WebSocket CONNECT rejected: Missing authorization token");
            throw new AccessDeniedException("Missing authorization token for WebSocket connection");
        }

        try {
            String username = jwtService.extractUsername(token);
            if (username != null) {
                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                    log.info("WebSocket connection authenticated for user: {} (Role: {})", 
                            username, userDetails.getUser().getRole());
                    return;
                }
            }
        } catch (Exception e) {
            log.error("WebSocket authentication failed: {}", e.getMessage());
        }

        throw new AccessDeniedException("Invalid JWT token for WebSocket connection");
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // 1. Try native headers "Authorization" or "token"
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header.startsWith("Bearer ")) {
                return header.substring(7);
            }
            return header;
        }

        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        // 2. Try session attributes (e.g. from handshake query params)
        if (accessor.getSessionAttributes() != null && accessor.getSessionAttributes().containsKey("token")) {
            return (String) accessor.getSessionAttributes().get("token");
        }

        return null;
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated subscription attempt");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found: " + principal.getName()));

        log.debug("Authorizing subscription to: {} for user: {} (Role: {})", destination, user.getEmail(), user.getRole());

        // Admin has access to all topics
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // 1. Emergency specific topic: /topic/emergency/{emergencyId}
        if (destination.startsWith("/topic/emergency/")) {
            String emergencyId = destination.substring("/topic/emergency/".length());
            Optional<EmergencyRequest> emOpt = emergencyRepository.findById(emergencyId);
            if (emOpt.isEmpty()) {
                throw new AccessDeniedException("Emergency not found: " + emergencyId);
            }
            EmergencyRequest emergency = emOpt.get();

            boolean authorized = false;
            if (user.getRole() == Role.PATIENT && user.getId().equals(emergency.getPatientId())) {
                authorized = true;
            } else if (user.getRole() == Role.AMBULANCE_DRIVER) {
                authorized = user.getId().equals(emergency.getAssignedDriverId()) ||
                        driverRepository.findByUserId(user.getId())
                                .map(d -> d.getId().equals(emergency.getAssignedDriverId()))
                                .orElse(false) ||
                        (emergency.getAssignedAmbulanceId() != null &&
                         ambulanceRepository.findById(emergency.getAssignedAmbulanceId())
                             .map(a -> a.getDriverId() != null && (
                                 a.getDriverId().equals(user.getId()) ||
                                 driverRepository.findByUserId(user.getId()).map(d -> d.getId().equals(a.getDriverId())).orElse(false)
                             )).orElse(false));
            } else if (user.getRole() == Role.HOSPITAL_STAFF) {
                authorized = user.getId().equals(emergency.getRecommendedHospitalId()) ||
                        hospitalRepository.findByUserId(user.getId())
                                .map(h -> h.getId().equals(emergency.getRecommendedHospitalId()))
                                .orElse(false);
            }

            if (!authorized) {
                log.warn("Subscription denied: User {} cannot subscribe to emergency {}", user.getEmail(), emergencyId);
                throw new AccessDeniedException("Not authorized to subscribe to this emergency");
            }
            return;
        }

        // 2. Patient topic: /topic/patient/{patientId}
        if (destination.startsWith("/topic/patient/")) {
            String patientId = destination.substring("/topic/patient/".length());
            boolean isPatient = user.getRole() == Role.PATIENT && (
                    user.getId().equals(patientId) ||
                    patientRepository.findByUserId(user.getId()).map(p -> p.getId().equals(patientId)).orElse(false)
            );
            if (!isPatient) {
                log.warn("Subscription denied: User {} cannot subscribe to patient {}", user.getEmail(), patientId);
                throw new AccessDeniedException("Not authorized to subscribe to this patient topic");
            }
            return;
        }

        // 3. Driver topic: /topic/driver/{driverId}
        if (destination.startsWith("/topic/driver/")) {
            String driverId = destination.substring("/topic/driver/".length());
            boolean isDriver = user.getRole() == Role.AMBULANCE_DRIVER && (
                    user.getId().equals(driverId) ||
                    driverRepository.findByUserId(user.getId()).map(d -> d.getId().equals(driverId)).orElse(false)
            );
            if (!isDriver) {
                log.warn("Subscription denied: User {} cannot subscribe to driver {}", user.getEmail(), driverId);
                throw new AccessDeniedException("Not authorized to subscribe to this driver topic");
            }
            return;
        }

        // 4. Hospital topic: /topic/hospital/{hospitalId}
        if (destination.startsWith("/topic/hospital/")) {
            String hospitalId = destination.substring("/topic/hospital/".length());
            boolean isHospital = user.getRole() == Role.HOSPITAL_STAFF && (
                    user.getId().equals(hospitalId) ||
                    hospitalRepository.findByUserId(user.getId()).map(h -> h.getId().equals(hospitalId)).orElse(false)
            );
            if (!isHospital) {
                log.warn("Subscription denied: User {} cannot subscribe to hospital {}", user.getEmail(), hospitalId);
                throw new AccessDeniedException("Not authorized to subscribe to this hospital topic");
            }
            return;
        }

        // 5. User notifications topic: /topic/user/{userId}/notifications
        if (destination.startsWith("/topic/user/")) {
            String[] parts = destination.split("/");
            if (parts.length >= 4 && parts[3].equals("notifications")) {
                String subUserId = parts[2];
                if (!user.getId().equals(subUserId)) {
                    log.warn("Subscription denied: User {} cannot subscribe to notifications for {}", user.getEmail(), subUserId);
                    throw new AccessDeniedException("Not authorized to subscribe to other user notifications");
                }
            }
            return;
        }

        // 6. Admin emergencies topic
        if (destination.startsWith("/topic/admin/")) {
            if (user.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Admin role required for admin topics");
            }
            return;
        }

        // Deny any other unknown protected destinations
        if (destination.startsWith("/topic/") || destination.startsWith("/queue/")) {
            log.warn("Subscription denied to unmapped destination: {}", destination);
            throw new AccessDeniedException("Destination not permitted: " + destination);
        }
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null && !destination.startsWith("/app/")) {
            log.warn("Client blocked from publishing directly to non-application destination: {}", destination);
            throw new AccessDeniedException("Publishing directly to " + destination + " is forbidden. Use /app destinations.");
        }
    }
}
