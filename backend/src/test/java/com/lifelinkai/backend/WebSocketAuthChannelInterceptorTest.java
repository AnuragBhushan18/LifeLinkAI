package com.lifelinkai.backend;

import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.AmbulanceRepository;
import com.lifelinkai.backend.repository.DriverRepository;
import com.lifelinkai.backend.repository.EmergencyRequestRepository;
import com.lifelinkai.backend.repository.HospitalRepository;
import com.lifelinkai.backend.repository.UserRepository;
import com.lifelinkai.backend.security.CustomUserDetails;
import com.lifelinkai.backend.security.JwtService;
import com.lifelinkai.backend.websocket.WebSocketAuthChannelInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebSocketAuthChannelInterceptorTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmergencyRequestRepository emergencyRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private AmbulanceRepository ambulanceRepository;
    @Mock
    private com.lifelinkai.backend.repository.PatientRepository patientRepository;
    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private WebSocketAuthChannelInterceptor interceptor;

    private User patientUser;
    private User driverUser;
    private User hospitalUser;
    private User otherUser;
    private CustomUserDetails patientDetails;

    @BeforeEach
    void setUp() {
        patientUser = new User("Patient", "patient@test.com", "pass", "123", Role.PATIENT);
        patientUser.setId("pat-1");

        driverUser = new User("Driver", "driver@test.com", "pass", "456", Role.AMBULANCE_DRIVER);
        driverUser.setId("drv-1");

        hospitalUser = new User("Hospital Staff", "hosp@test.com", "pass", "789", Role.HOSPITAL_STAFF);
        hospitalUser.setId("hosp-1");

        otherUser = new User("Other", "other@test.com", "pass", "000", Role.PATIENT);
        otherUser.setId("other-1");

        patientDetails = new CustomUserDetails(patientUser);
    }

    @Test
    void testConnectWithValidToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid.jwt.token");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("patient@test.com");
        when(userDetailsService.loadUserByUsername("patient@test.com")).thenReturn(patientDetails);
        when(jwtService.isTokenValid("valid.jwt.token", patientDetails)).thenReturn(true);

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);

        StompHeaderAccessor resAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resAccessor.getUser());
        assertEquals("patient@test.com", resAccessor.getUser().getName());
    }

    @Test
    void testConnectWithMissingToken_ThrowsAccessDenied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void testPatientSubscriptionToOwnEmergency_Allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/emergency/em-1");
        Principal principal = new UsernamePasswordAuthenticationToken(patientUser.getEmail(), null);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        EmergencyRequest em = EmergencyRequest.builder().id("em-1").patientId("pat-1").build();
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));
        when(emergencyRepository.findById("em-1")).thenReturn(Optional.of(em));

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }

    @Test
    void testPatientSubscriptionToOtherEmergency_Denied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/emergency/em-2");
        Principal principal = new UsernamePasswordAuthenticationToken(otherUser.getEmail(), null);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        EmergencyRequest em = EmergencyRequest.builder().id("em-2").patientId("pat-1").build();
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(emergencyRepository.findById("em-2")).thenReturn(Optional.of(em));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void testDriverSubscriptionToAssignedEmergency_Allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/emergency/em-1");
        Principal principal = new UsernamePasswordAuthenticationToken(driverUser.getEmail(), null);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        EmergencyRequest em = EmergencyRequest.builder().id("em-1").assignedDriverId("drv-1").build();
        when(userRepository.findByEmail("driver@test.com")).thenReturn(Optional.of(driverUser));
        when(emergencyRepository.findById("em-1")).thenReturn(Optional.of(em));

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }

    @Test
    void testHospitalSubscriptionToAssignedEmergency_Allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/emergency/em-1");
        Principal principal = new UsernamePasswordAuthenticationToken(hospitalUser.getEmail(), null);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        EmergencyRequest em = EmergencyRequest.builder().id("em-1").recommendedHospitalId("hosp-1").build();
        when(userRepository.findByEmail("hosp@test.com")).thenReturn(Optional.of(hospitalUser));
        when(emergencyRepository.findById("em-1")).thenReturn(Optional.of(em));

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }

    @Test
    void testDirectPublishToTopic_Denied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/topic/emergency/em-1");
        Principal principal = new UsernamePasswordAuthenticationToken(patientUser.getEmail(), null);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void testPublishToAppDestination_Allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/ambulance/location");
        Principal principal = new UsernamePasswordAuthenticationToken(driverUser.getEmail(), null);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }
}
