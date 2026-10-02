package com.lifelinkai.backend;

import com.lifelinkai.backend.dto.AmbulanceLocationRequest;
import com.lifelinkai.backend.dto.AmbulanceLocationResponse;
import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.*;
import com.lifelinkai.backend.service.AmbulanceLocationService;
import com.lifelinkai.backend.service.DistanceService;
import com.lifelinkai.backend.websocket.RealtimeEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AmbulanceLocationServiceTest {

    @Mock
    private AmbulanceRepository ambulanceRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private EmergencyRequestRepository emergencyRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DistanceService distanceService;
    @Mock
    private RealtimeEventPublisher realtimeEventPublisher;

    @InjectMocks
    private AmbulanceLocationService locationService;

    private User driverUser;
    private Driver driver;
    private Ambulance ambulance;
    private EmergencyRequest emergency;

    @BeforeEach
    void setUp() {
        driverUser = new User("Driver John", "driver@test.com", "pass", "123", Role.AMBULANCE_DRIVER);
        driverUser.setId("usr-driver-1");

        driver = new Driver();
        driver.setId("drv-1");
        driver.setUserId("usr-driver-1");

        ambulance = new Ambulance();
        ambulance.setId("amb-1");
        ambulance.setDriverId("drv-1");
        ambulance.setVehicleNumber("AMB-101");
        ambulance.setLatitude(40.7128);
        ambulance.setLongitude(-74.0060);

        emergency = EmergencyRequest.builder()
                .id("em-1")
                .patientId("pat-1")
                .latitude(40.7300)
                .longitude(-73.9950)
                .status(EmergencyStatus.GOING_TO_PATIENT)
                .assignedAmbulanceId("amb-1")
                .assignedDriverId("drv-1")
                .build();
    }

    @Test
    void testUpdateDriverLocation_Success() {
        when(userRepository.findByEmail("driver@test.com")).thenReturn(Optional.of(driverUser));
        when(driverRepository.findByUserId("usr-driver-1")).thenReturn(Optional.of(driver));
        when(ambulanceRepository.findFirstByDriverId("drv-1")).thenReturn(Optional.of(ambulance));
        when(emergencyRepository.findAll()).thenReturn(List.of(emergency));
        when(distanceService.calculateDistanceKm(40.7200, -74.0000, 40.7300, -73.9950)).thenReturn(1.5);
        when(distanceService.estimateEtaMinutes(1.5)).thenReturn(3.0);

        AmbulanceLocationRequest request = AmbulanceLocationRequest.builder()
                .latitude(40.7200)
                .longitude(-74.0000)
                .simulated(false)
                .build();

        AmbulanceLocationResponse response = locationService.updateDriverLocation("driver@test.com", request);

        assertNotNull(response);
        assertEquals(40.7200, response.getLatitude());
        assertEquals(-74.0000, response.getLongitude());
        assertEquals(1.5, response.getDistanceKm());
        assertEquals(3.0, response.getEtaMinutes());
        assertEquals("amb-1", response.getAmbulanceId());
        assertEquals("em-1", response.getEmergencyId());

        verify(ambulanceRepository, times(1)).save(ambulance);
        verify(emergencyRepository, times(1)).save(emergency);
        verify(realtimeEventPublisher, times(1)).publishLocationUpdate(any(), any());
    }

    @Test
    void testNonDriverUser_ThrowsSecurityException() {
        User patientUser = new User("Patient", "patient@test.com", "pass", "123", Role.PATIENT);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patientUser));

        AmbulanceLocationRequest request = AmbulanceLocationRequest.builder()
                .latitude(40.7200)
                .longitude(-74.0000)
                .build();

        assertThrows(SecurityException.class, () -> locationService.updateDriverLocation("patient@test.com", request));
    }

    @Test
    void testInvalidCoordinates_ThrowsIllegalArgumentException() {
        when(userRepository.findByEmail("driver@test.com")).thenReturn(Optional.of(driverUser));

        AmbulanceLocationRequest request = AmbulanceLocationRequest.builder()
                .latitude(95.0) // invalid
                .longitude(-74.0000)
                .build();

        assertThrows(IllegalArgumentException.class, () -> locationService.updateDriverLocation("driver@test.com", request));
    }

    @Test
    void testNoActiveEmergency_ThrowsIllegalStateException() {
        when(userRepository.findByEmail("driver@test.com")).thenReturn(Optional.of(driverUser));
        when(driverRepository.findByUserId("usr-driver-1")).thenReturn(Optional.of(driver));
        when(ambulanceRepository.findFirstByDriverId("drv-1")).thenReturn(Optional.of(ambulance));
        when(emergencyRepository.findAll()).thenReturn(List.of()); // No active emergencies

        AmbulanceLocationRequest request = AmbulanceLocationRequest.builder()
                .latitude(40.7200)
                .longitude(-74.0000)
                .build();

        assertThrows(IllegalStateException.class, () -> locationService.updateDriverLocation("driver@test.com", request));
    }
}
