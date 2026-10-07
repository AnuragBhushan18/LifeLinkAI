package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.AmbulanceLocationRequest;
import com.lifelinkai.backend.dto.AmbulanceLocationResponse;
import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.*;
import com.lifelinkai.backend.websocket.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmbulanceLocationService {

    private final AmbulanceRepository ambulanceRepository;
    private final DriverRepository driverRepository;
    private final EmergencyRequestRepository emergencyRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final DistanceService distanceService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    // Rate limiting map: ambulanceId -> timestamp of last update
    private final Map<String, Long> lastUpdateTimestamps = new ConcurrentHashMap<>();
    private static final long MIN_UPDATE_INTERVAL_MS = 1000; // at least 1 second between writes

    public AmbulanceLocationResponse updateDriverLocation(String userEmail, AmbulanceLocationRequest request) {
        // 1. Authenticate driver
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        if (user.getRole() != Role.AMBULANCE_DRIVER && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Unauthorized: Only ambulance drivers can send location updates");
        }

        // 2. Validate coordinates
        if (request.getLatitude() < -90.0 || request.getLatitude() > 90.0 ||
            request.getLongitude() < -180.0 || request.getLongitude() > 180.0) {
            throw new IllegalArgumentException("Invalid latitude or longitude coordinates");
        }

        // 3. Find driver profile
        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Create default driver record if missing
                    Driver d = new Driver();
                    d.setUserId(user.getId());
                    d.setAvailability(Availability.AVAILABLE);
                    return driverRepository.save(d);
                });

        // 4. Verify driver is associated with an ambulance
        Ambulance ambulance = ambulanceRepository.findFirstByDriverId(driver.getId())
                .or(() -> ambulanceRepository.findFirstByDriverId(user.getId()))
                .or(() -> ambulanceRepository.findAll().stream()
                        .filter(a -> a.getDriverId() == null || a.getDriverId().equals(driver.getId()) || a.getDriverId().equals(user.getId()))
                        .findFirst()
                        .map(a -> {
                            a.setDriverId(driver.getId());
                            return ambulanceRepository.save(a);
                        }))
                .orElseThrow(() -> new IllegalStateException("No ambulance associated with authenticated driver"));

        // 5. Verify the ambulance has an active emergency assignment
        List<EmergencyRequest> activeEmergencies = emergencyRepository.findAll().stream()
                .filter(e -> {
                    boolean matchesAmbulance = ambulance.getId().equals(e.getAssignedAmbulanceId());
                    boolean matchesDriver = (driver.getId().equals(e.getAssignedDriverId()) || user.getId().equals(e.getAssignedDriverId()));
                    boolean isActive = e.getStatus() != null && 
                            e.getStatus() != EmergencyStatus.COMPLETED &&
                            e.getStatus() != EmergencyStatus.CANCELLED &&
                            e.getStatus() != EmergencyStatus.REJECTED;
                    return (matchesAmbulance || matchesDriver) && isActive;
                })
                .toList();

        if (activeEmergencies.isEmpty()) {
            throw new IllegalStateException("Ambulance " + ambulance.getVehicleNumber() + " has no active emergency assignment");
        }

        EmergencyRequest emergency = activeEmergencies.get(0);

        // 6. Rate-limiting check
        long now = System.currentTimeMillis();
        Long lastTime = lastUpdateTimestamps.get(ambulance.getId());
        if (lastTime != null && (now - lastTime) < MIN_UPDATE_INTERVAL_MS) {
            log.debug("Rate-limiting location update for ambulance {}", ambulance.getId());
            // Return cached / computed response without duplicate DB persist
            return buildResponse(ambulance, emergency, request.getSimulated());
        }
        lastUpdateTimestamps.put(ambulance.getId(), now);

        // 7. Update Ambulance location in MongoDB
        ambulance.setLatitude(request.getLatitude());
        ambulance.setLongitude(request.getLongitude());
        ambulance.setUpdatedAt(LocalDateTime.now());
        ambulanceRepository.save(ambulance);

        // 8. Calculate ETA and remaining distance
        double remainingDistance = 0.0;
        double remainingEta = 0.0;

        if (emergency.getStatus() == EmergencyStatus.GOING_TO_PATIENT) {
            remainingDistance = distanceService.calculateDistanceKm(
                    request.getLatitude(), request.getLongitude(),
                    emergency.getLatitude(), emergency.getLongitude());
            remainingEta = distanceService.estimateEtaMinutes(remainingDistance);
        } else if (emergency.getStatus() == EmergencyStatus.GOING_TO_HOSPITAL) {
            if (emergency.getRecommendedHospitalId() != null) {
                Hospital hospital = hospitalRepository.findById(emergency.getRecommendedHospitalId())
                        .or(() -> hospitalRepository.findByUserId(emergency.getRecommendedHospitalId()))
                        .orElse(null);
                if (hospital != null && hospital.getLatitude() != null && hospital.getLongitude() != null) {
                    remainingDistance = distanceService.calculateDistanceKm(
                            request.getLatitude(), request.getLongitude(),
                            hospital.getLatitude(), hospital.getLongitude());
                    remainingEta = distanceService.estimateEtaMinutes(remainingDistance);
                }
            }
        }

        // 9. Update emergency snapshot
        emergency.setCurrentAmbulanceLatitude(request.getLatitude());
        emergency.setCurrentAmbulanceLongitude(request.getLongitude());
        emergency.setEstimatedDistanceKm(Math.round(remainingDistance * 10.0) / 10.0);
        emergency.setEstimatedEtaMinutes(Math.round(remainingEta * 10.0) / 10.0);
        emergency.setUpdatedAt(LocalDateTime.now());
        emergencyRepository.save(emergency);

        // 10. Build response and broadcast
        AmbulanceLocationResponse response = AmbulanceLocationResponse.builder()
                .ambulanceId(ambulance.getId())
                .emergencyId(emergency.getId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .distanceKm(emergency.getEstimatedDistanceKm())
                .etaMinutes(emergency.getEstimatedEtaMinutes())
                .simulated(request.getSimulated())
                .timestamp(LocalDateTime.now())
                .build();

        realtimeEventPublisher.publishLocationUpdate(response, emergency);
        log.info("Broadcasted location for ambulance {}: lat={}, lon={}, ETA={}min, simulated={}", 
                ambulance.getId(), request.getLatitude(), request.getLongitude(), emergency.getEstimatedEtaMinutes(), request.getSimulated());

        return response;
    }

    private AmbulanceLocationResponse buildResponse(Ambulance ambulance, EmergencyRequest emergency, Boolean simulated) {
        return AmbulanceLocationResponse.builder()
                .ambulanceId(ambulance.getId())
                .emergencyId(emergency.getId())
                .latitude(ambulance.getLatitude())
                .longitude(ambulance.getLongitude())
                .distanceKm(emergency.getEstimatedDistanceKm())
                .etaMinutes(emergency.getEstimatedEtaMinutes())
                .simulated(simulated)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
