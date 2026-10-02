package com.lifelinkai.backend;

import com.lifelinkai.backend.dto.AmbulanceAllocationResponse;
import com.lifelinkai.backend.dto.AmbulanceLocationRequest;
import com.lifelinkai.backend.dto.EmergencyCreateRequest;
import com.lifelinkai.backend.dto.EmergencySeverityResult;
import com.lifelinkai.backend.dto.HospitalRecommendationResponse;
import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.*;
import com.lifelinkai.backend.service.AmbulanceAllocationService;
import com.lifelinkai.backend.service.AmbulanceLocationService;
import com.lifelinkai.backend.service.EmergencyService;
import com.lifelinkai.backend.service.EmergencySeverityService;
import com.lifelinkai.backend.service.HospitalRecommendationService;
import com.lifelinkai.backend.websocket.RealtimeEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EmergencyRealtimeIntegrationTest {

    @Autowired
    private EmergencyService emergencyService;

    @Autowired
    private AmbulanceLocationService ambulanceLocationService;

    @Autowired
    private EmergencyRequestRepository emergencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private RealtimeEventPublisher realtimeEventPublisher;

    private User patient;
    private User driverUser;
    private Driver driver;
    private Ambulance ambulance;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        emergencyRepository.deleteAll();
        ambulanceRepository.deleteAll();
        driverRepository.deleteAll();
        hospitalRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Patient User
        patient = new User("Jane Doe", "jane@test.com", "pass", "9998887771", Role.PATIENT);
        patient = userRepository.save(patient);

        // 2. Create Driver User & Profile
        driverUser = new User("Driver Dave", "dave@test.com", "pass", "9998887772", Role.AMBULANCE_DRIVER);
        driverUser = userRepository.save(driverUser);

        driver = new Driver();
        driver.setUserId(driverUser.getId());
        driver.setLicenseNumber("DL-12345");
        driver.setAvailability(Availability.AVAILABLE);
        driver = driverRepository.save(driver);

        // 3. Create Hospital
        hospital = new Hospital();
        hospital.setName("Metro General Hospital");
        hospital.setLatitude(40.7128);
        hospital.setLongitude(-74.0060);
        hospital.setAvailableBeds(15);
        hospital.setAvailableIcuBeds(4);
        hospital.setOperationalStatus(OperationalStatus.ACTIVE);
        hospital = hospitalRepository.save(hospital);

        // 4. Create Ambulance
        ambulance = new Ambulance();
        ambulance.setVehicleNumber("METRO-01");
        ambulance.setHospitalId(hospital.getId());
        ambulance.setDriverId(driver.getId());
        ambulance.setType(AmbulanceType.ICU);
        ambulance.setStatus(AmbulanceStatus.IDLE);
        ambulance.setLatitude(40.7100);
        ambulance.setLongitude(-74.0050);
        ambulance = ambulanceRepository.save(ambulance);
    }

    @Test
    void testFullRealtimeEmergencyWorkflow() {
        // Step 1: Patient creates Emergency SOS
        EmergencyCreateRequest createRequest = new EmergencyCreateRequest();
        createRequest.setLatitude(40.7200);
        createRequest.setLongitude(-74.0100);
        createRequest.setSymptoms(List.of("difficulty breathing", "chest pain"));
        createRequest.setEmergencyDescription("Critical cardiac emergency");

        EmergencyRequest emergency = emergencyService.createEmergency(patient.getId(), createRequest);

        assertNotNull(emergency);
        assertNotNull(emergency.getId());
        // Phase 3 deterministic engine runs synchronously upon creation
        assertEquals(EmergencyStatus.AMBULANCE_ASSIGNED, emergency.getStatus());
        assertEquals(EmergencySeverity.CRITICAL, emergency.getSeverity());
        assertEquals(hospital.getId(), emergency.getRecommendedHospitalId());
        assertEquals(ambulance.getId(), emergency.getAssignedAmbulanceId());

        // Verify Timeline was recorded
        assertNotNull(emergency.getTimeline());
        assertTrue(emergency.getTimeline().size() >= 3);
        assertTrue(emergency.getTimeline().stream().anyMatch(t -> t.getTitle().contains("SOS Created")));
        assertTrue(emergency.getTimeline().stream().anyMatch(t -> t.getTitle().contains("Severity Analyzed")));
        assertTrue(emergency.getTimeline().stream().anyMatch(t -> t.getTitle().contains("Ambulance Dispatched")));

        // Verify Notifications were persisted
        List<Notification> patientNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(patient.getId());
        assertFalse(patientNotifs.isEmpty(), "Patient should receive notifications");

        List<Notification> driverNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(driverUser.getId());
        assertFalse(driverNotifs.isEmpty(), "Driver should receive dispatch notification");

        // Verify Real-time Broadcast was called
        verify(realtimeEventPublisher, atLeastOnce()).publishEmergencyEvent(any(), any());

        // Step 2: Driver sends location update while en route
        emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.GOING_TO_PATIENT);

        AmbulanceLocationRequest locationReq = AmbulanceLocationRequest.builder()
                .latitude(40.7150)
                .longitude(-74.0080)
                .simulated(true)
                .build();

        var locationRes = ambulanceLocationService.updateDriverLocation(driverUser.getEmail(), locationReq);

        assertNotNull(locationRes);
        assertEquals(40.7150, locationRes.getLatitude());
        assertEquals(-74.0080, locationRes.getLongitude());
        assertTrue(locationRes.getDistanceKm() > 0);
        assertTrue(locationRes.getEtaMinutes() > 0);
        assertTrue(locationRes.getSimulated());

        // Verify location broadcasted
        verify(realtimeEventPublisher, atLeastOnce()).publishLocationUpdate(any(), any());

        // Verify Emergency snapshot updated
        EmergencyRequest updatedEmergency = emergencyRepository.findById(emergency.getId()).orElseThrow();
        assertEquals(40.7150, updatedEmergency.getCurrentAmbulanceLatitude());
        assertEquals(-74.0080, updatedEmergency.getCurrentAmbulanceLongitude());
        assertNotNull(updatedEmergency.getEstimatedEtaMinutes());

        // Step 3: Status progression through state machine
        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.ARRIVED_AT_PICKUP);
        assertEquals(EmergencyStatus.ARRIVED_AT_PICKUP, emergency.getStatus());

        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.PATIENT_PICKED_UP);
        assertEquals(EmergencyStatus.PATIENT_PICKED_UP, emergency.getStatus());

        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.GOING_TO_HOSPITAL);
        assertEquals(EmergencyStatus.GOING_TO_HOSPITAL, emergency.getStatus());

        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.ARRIVED_AT_HOSPITAL);
        assertEquals(EmergencyStatus.ARRIVED_AT_HOSPITAL, emergency.getStatus());

        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.ADMITTED);
        assertEquals(EmergencyStatus.ADMITTED, emergency.getStatus());

        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.TREATMENT_IN_PROGRESS);
        assertEquals(EmergencyStatus.TREATMENT_IN_PROGRESS, emergency.getStatus());

        emergency = emergencyService.updateEmergencyStatus(emergency.getId(), EmergencyStatus.COMPLETED);
        assertEquals(EmergencyStatus.COMPLETED, emergency.getStatus());
        assertNotNull(emergency.getCompletedAt());

        final String emergencyId = emergency.getId();
        // Verify invalid state transition is rejected
        assertThrows(RuntimeException.class, () -> 
                emergencyService.updateEmergencyStatus(emergencyId, EmergencyStatus.GOING_TO_PATIENT));
    }
}
