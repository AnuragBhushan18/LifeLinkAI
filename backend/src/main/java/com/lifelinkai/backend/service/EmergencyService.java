package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.AmbulanceAllocationResponse;
import com.lifelinkai.backend.dto.EmergencyCreateRequest;
import com.lifelinkai.backend.dto.EmergencyEventDto;
import com.lifelinkai.backend.dto.EmergencySeverityResult;
import com.lifelinkai.backend.dto.HospitalRecommendationResponse;
import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.*;
import com.lifelinkai.backend.websocket.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyService {

    private final EmergencyRequestRepository emergencyRepository;
    private final EmergencySeverityService severityService;
    private final HospitalRecommendationService hospitalService;
    private final AmbulanceAllocationService ambulanceService;
    private final EmergencyStateService stateService;
    private final AmbulanceRepository ambulanceRepository;
    private final DriverRepository driverRepository;
    private final HospitalRepository hospitalRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final NotificationService notificationService;

    public EmergencyRequest createEmergency(String patientId, EmergencyCreateRequest request) {
        log.info("Creating new emergency for patient: {}", patientId);
        EmergencyRequest emergency = EmergencyRequest.builder()
                .patientId(patientId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .symptoms(request.getSymptoms())
                .emergencyDescription(request.getEmergencyDescription())
                .status(EmergencyStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .timeline(new ArrayList<>())
                .build();

        addTimelineEvent(emergency, EmergencyStatus.CREATED, "SOS Created", "Emergency SOS triggered by patient");
        emergency = emergencyRepository.save(emergency);

        notificationService.createNotification(
                patientId,
                NotificationType.SOS_CREATED,
                "Emergency SOS Activated",
                "Your emergency request has been received. Locating nearest hospital and ambulance.",
                emergency.getId()
        );

        broadcastEmergencyUpdate(emergency, "SOS Emergency initiated");

        // Execute deterministic engine
        return processEmergency(emergency.getId());
    }

    public EmergencyRequest processEmergency(String id) {
        EmergencyRequest emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency not found: " + id));

        // 1. Severity Analysis
        if (emergency.getStatus() == EmergencyStatus.CREATED) {
            EmergencySeverityResult severityResult = severityService.analyzeSeverity(
                    emergency.getSymptoms(), emergency.getEmergencyDescription());

            emergency.setSeverity(severityResult.getSeverity());
            emergency.setSeverityFactors(severityResult.getSeverityFactors());

            updateState(emergency, EmergencyStatus.SEVERITY_ANALYZED);
            addTimelineEvent(emergency, EmergencyStatus.SEVERITY_ANALYZED, "Severity Analyzed",
                    "Assessed as " + severityResult.getSeverity() + " based on symptoms");
            broadcastEmergencyUpdate(emergency, "Severity analyzed: " + severityResult.getSeverity());
        }

        // 2. Hospital Recommendation
        if (emergency.getStatus() == EmergencyStatus.SEVERITY_ANALYZED) {
            HospitalRecommendationResponse hospitalRes = hospitalService.recommendHospital(
                    emergency.getLatitude(), emergency.getLongitude());

            if (hospitalRes != null) {
                emergency.setRecommendedHospitalId(hospitalRes.getHospitalId());
                updateState(emergency, EmergencyStatus.AMBULANCE_SEARCHING);
                addTimelineEvent(emergency, EmergencyStatus.AMBULANCE_SEARCHING, "Hospital Recommended",
                        "Recommended: " + hospitalRes.getHospitalName() + " (ETA ~" + hospitalRes.getEstimatedEtaMinutes() + " mins)");

                // Notify Hospital
                String hospitalUserId = getHospitalUserId(hospitalRes.getHospitalId());
                notificationService.createNotification(
                        hospitalUserId,
                        NotificationType.EMERGENCY_STATUS_UPDATED,
                        "Incoming Emergency Assigned",
                        "Emergency (" + emergency.getSeverity() + ") recommended to your hospital. ETA ~" + hospitalRes.getEstimatedEtaMinutes() + " min",
                        emergency.getId()
                );

                broadcastEmergencyUpdate(emergency, "Hospital assigned: " + hospitalRes.getHospitalName());
            }
        }

        // 3. Ambulance Allocation
        if (emergency.getStatus() == EmergencyStatus.AMBULANCE_SEARCHING) {
            AmbulanceAllocationResponse ambulanceRes = ambulanceService.allocateAmbulance(
                    emergency.getLatitude(), emergency.getLongitude(), emergency.getSeverity());

            if (ambulanceRes != null) {
                emergency.setAssignedAmbulanceId(ambulanceRes.getAmbulanceId());
                emergency.setAssignedDriverId(ambulanceRes.getDriverId());
                emergency.setAmbulanceAssignedAt(LocalDateTime.now());
                emergency.setEstimatedDistanceKm(ambulanceRes.getDistanceKm());
                emergency.setEstimatedEtaMinutes(ambulanceRes.getEstimatedEtaMinutes());

                // Update Ambulance state
                Ambulance ambulance = ambulanceRepository.findById(ambulanceRes.getAmbulanceId()).orElse(null);
                if (ambulance != null) {
                    ambulance.setStatus(AmbulanceStatus.ASSIGNED);
                    ambulanceRepository.save(ambulance);
                    emergency.setCurrentAmbulanceLatitude(ambulance.getLatitude());
                    emergency.setCurrentAmbulanceLongitude(ambulance.getLongitude());
                }

                updateState(emergency, EmergencyStatus.AMBULANCE_ASSIGNED);
                addTimelineEvent(emergency, EmergencyStatus.AMBULANCE_ASSIGNED, "Ambulance Dispatched",
                        "Ambulance " + ambulanceRes.getVehicleNumber() + " assigned (ETA ~" + ambulanceRes.getEstimatedEtaMinutes() + " mins)");

                // Notify Patient
                notificationService.createNotification(
                        emergency.getPatientId(),
                        NotificationType.AMBULANCE_ASSIGNED,
                        "Ambulance Assigned",
                        "Ambulance " + ambulanceRes.getVehicleNumber() + " has been dispatched to your location.",
                        emergency.getId()
                );

                // Notify Driver
                String driverUserId = getDriverUserId(ambulanceRes.getDriverId());
                notificationService.createNotification(
                        driverUserId,
                        NotificationType.AMBULANCE_ASSIGNED,
                        "Emergency Dispatch Assignment",
                        "You have been assigned an emergency dispatch (" + emergency.getSeverity() + ").",
                        emergency.getId()
                );

                broadcastEmergencyUpdate(emergency, "Ambulance dispatched: " + ambulanceRes.getVehicleNumber());
            }
        }

        return emergencyRepository.save(emergency);
    }

    public EmergencyRequest updateEmergencyStatus(String id, EmergencyStatus newStatus) {
        EmergencyRequest emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency not found: " + id));

        if (!stateService.isValidTransition(emergency.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid state transition from " + emergency.getStatus() + " to " + newStatus);
        }

        emergency.setStatus(newStatus);
        emergency.setUpdatedAt(LocalDateTime.now());

        if (newStatus == EmergencyStatus.COMPLETED) {
            emergency.setCompletedAt(LocalDateTime.now());
            // Release ambulance
            if (emergency.getAssignedAmbulanceId() != null) {
                ambulanceRepository.findById(emergency.getAssignedAmbulanceId()).ifPresent(a -> {
                    a.setStatus(AmbulanceStatus.IDLE);
                    ambulanceRepository.save(a);
                });
            }
        } else if (newStatus == EmergencyStatus.CANCELLED) {
            // Release ambulance
            if (emergency.getAssignedAmbulanceId() != null) {
                ambulanceRepository.findById(emergency.getAssignedAmbulanceId()).ifPresent(a -> {
                    a.setStatus(AmbulanceStatus.IDLE);
                    ambulanceRepository.save(a);
                });
            }
        }

        String eventMessage = getStatusMessage(newStatus);
        addTimelineEvent(emergency, newStatus, getStatusTitle(newStatus), eventMessage);

        emergency = emergencyRepository.save(emergency);

        // Send notifications based on new status
        notifyStatusChange(emergency, newStatus, eventMessage);

        // Broadcast real-time event to all subscribers
        broadcastEmergencyUpdate(emergency, eventMessage);

        log.info("Emergency {} updated to status {} - event broadcasted", id, newStatus);
        return emergency;
    }

    private void notifyStatusChange(EmergencyRequest emergency, EmergencyStatus status, String message) {
        String patientId = emergency.getPatientId();
        String driverUserId = getDriverUserId(emergency.getAssignedDriverId());
        String hospitalUserId = getHospitalUserId(emergency.getRecommendedHospitalId());

        switch (status) {
            case GOING_TO_PATIENT -> {
                notificationService.createNotification(patientId, NotificationType.AMBULANCE_APPROACHING,
                        "Ambulance En Route", "Ambulance is heading to your pickup location.", emergency.getId());
            }
            case ARRIVED_AT_PICKUP -> {
                notificationService.createNotification(patientId, NotificationType.AMBULANCE_ARRIVED,
                        "Ambulance Arrived", "Ambulance has arrived at the pickup location.", emergency.getId());
            }
            case PATIENT_PICKED_UP -> {
                notificationService.createNotification(patientId, NotificationType.PATIENT_PICKED_UP,
                        "Patient Boarded", "Patient picked up. Ambulance heading to hospital.", emergency.getId());
                notificationService.createNotification(hospitalUserId, NotificationType.PATIENT_PICKED_UP,
                        "Patient In Transit", "Patient picked up by ambulance, en route to hospital.", emergency.getId());
            }
            case GOING_TO_HOSPITAL -> {
                notificationService.createNotification(hospitalUserId, NotificationType.AMBULANCE_APPROACHING,
                        "Ambulance En Route to Hospital", "Ambulance is transporting patient to your hospital.", emergency.getId());
            }
            case ARRIVED_AT_HOSPITAL -> {
                notificationService.createNotification(hospitalUserId, NotificationType.HOSPITAL_ARRIVAL,
                        "Ambulance Arrived at Hospital", "Ambulance has arrived at your emergency facility.", emergency.getId());
                notificationService.createNotification(patientId, NotificationType.HOSPITAL_ARRIVAL,
                        "Arrived at Hospital", "You have arrived at the hospital facility.", emergency.getId());
            }
            case ADMITTED -> {
                notificationService.createNotification(patientId, NotificationType.HOSPITAL_ACCEPTED,
                        "Patient Admitted", "Patient has been admitted for emergency care.", emergency.getId());
            }
            case TREATMENT_IN_PROGRESS -> {
                notificationService.createNotification(patientId, NotificationType.EMERGENCY_STATUS_UPDATED,
                        "Treatment In Progress", "Medical team is administering treatment.", emergency.getId());
            }
            case COMPLETED -> {
                notificationService.createNotification(patientId, NotificationType.EMERGENCY_COMPLETED,
                        "Emergency Completed", "Emergency treatment has been completed.", emergency.getId());
                notificationService.createNotification(driverUserId, NotificationType.EMERGENCY_COMPLETED,
                        "Dispatch Complete", "Emergency dispatch completed. Ambulance is now IDLE.", emergency.getId());
            }
            case CANCELLED -> {
                notificationService.createNotification(driverUserId, NotificationType.EMERGENCY_STATUS_UPDATED,
                        "Emergency Cancelled", "The emergency was cancelled by the patient.", emergency.getId());
                notificationService.createNotification(hospitalUserId, NotificationType.EMERGENCY_STATUS_UPDATED,
                        "Emergency Cancelled", "The incoming emergency was cancelled.", emergency.getId());
            }
            default -> {}
        }
    }

    private void addTimelineEvent(EmergencyRequest emergency, EmergencyStatus status, String title, String description) {
        if (emergency.getTimeline() == null) {
            emergency.setTimeline(new ArrayList<>());
        }
        EmergencyTimelineEvent event = EmergencyTimelineEvent.builder()
                .id(UUID.randomUUID().toString())
                .status(status)
                .title(title)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();
        emergency.getTimeline().add(event);
    }

    public void broadcastEmergencyUpdate(EmergencyRequest emergency, String message) {
        EmergencyEventDto event = EmergencyEventDto.builder()
                .eventType("EMERGENCY_STATUS_UPDATED")
                .emergencyId(emergency.getId())
                .status(emergency.getStatus())
                .severity(emergency.getSeverity())
                .timestamp(LocalDateTime.now())
                .message(message)
                .latitude(emergency.getCurrentAmbulanceLatitude())
                .longitude(emergency.getCurrentAmbulanceLongitude())
                .etaMinutes(emergency.getEstimatedEtaMinutes())
                .distanceKm(emergency.getEstimatedDistanceKm())
                .ambulanceId(emergency.getAssignedAmbulanceId())
                .driverId(emergency.getAssignedDriverId())
                .hospitalId(emergency.getRecommendedHospitalId())
                .timeline(emergency.getTimeline())
                .build();

        realtimeEventPublisher.publishEmergencyEvent(event, emergency);
    }

    private String getStatusTitle(EmergencyStatus status) {
        return switch (status) {
            case CREATED -> "Emergency Created";
            case SEVERITY_ANALYZED -> "Severity Analyzed";
            case AMBULANCE_SEARCHING -> "Searching Ambulance";
            case AMBULANCE_ASSIGNED -> "Ambulance Assigned";
            case GOING_TO_PATIENT -> "En Route to Patient";
            case ARRIVED_AT_PICKUP -> "Arrived at Pickup";
            case PATIENT_PICKED_UP -> "Patient Picked Up";
            case GOING_TO_HOSPITAL -> "En Route to Hospital";
            case ARRIVED_AT_HOSPITAL -> "Arrived at Hospital";
            case BED_RESERVED -> "Bed Reserved";
            case ADMITTED -> "Patient Admitted";
            case TREATMENT_IN_PROGRESS -> "Treatment in Progress";
            case COMPLETED -> "Emergency Completed";
            case CANCELLED -> "Emergency Cancelled";
            case REJECTED -> "Emergency Rejected";
        };
    }

    private String getStatusMessage(EmergencyStatus status) {
        return switch (status) {
            case CREATED -> "Emergency SOS activated by patient.";
            case SEVERITY_ANALYZED -> "Emergency severity has been analyzed.";
            case AMBULANCE_SEARCHING -> "Searching for suitable ambulance.";
            case AMBULANCE_ASSIGNED -> "Ambulance has been assigned.";
            case GOING_TO_PATIENT -> "Ambulance is on the way to the patient.";
            case ARRIVED_AT_PICKUP -> "Ambulance has arrived at the pickup location.";
            case PATIENT_PICKED_UP -> "Patient safely boarded into ambulance.";
            case GOING_TO_HOSPITAL -> "Ambulance is transporting patient to hospital.";
            case ARRIVED_AT_HOSPITAL -> "Ambulance has arrived at hospital.";
            case BED_RESERVED -> "A hospital bed has been reserved for the patient.";
            case ADMITTED -> "Patient admitted to the hospital facility.";
            case TREATMENT_IN_PROGRESS -> "Treatment is actively in progress.";
            case COMPLETED -> "Emergency resolved and concluded.";
            case CANCELLED -> "Emergency request was cancelled.";
            case REJECTED -> "Emergency request was rejected.";
        };
    }

    private String getDriverUserId(String driverId) {
        if (driverId == null) return null;
        return driverRepository.findById(driverId)
                .map(Driver::getUserId)
                .orElse(driverId);
    }

    private String getHospitalUserId(String hospitalId) {
        if (hospitalId == null) return null;
        return hospitalRepository.findById(hospitalId)
                .map(Hospital::getUserId)
                .orElse(hospitalId);
    }

    private void updateState(EmergencyRequest emergency, EmergencyStatus newStatus) {
        if (stateService.isValidTransition(emergency.getStatus(), newStatus)) {
            emergency.setStatus(newStatus);
            emergency.setUpdatedAt(LocalDateTime.now());
        }
    }

    public EmergencyRequest getEmergency(String id) {
        return emergencyRepository.findById(id).orElseThrow(() -> new RuntimeException("Emergency not found: " + id));
    }

    public List<EmergencyRequest> getAllEmergencies() {
        return emergencyRepository.findAll();
    }

    public List<EmergencyRequest> getPatientEmergencies(String patientId) {
        return emergencyRepository.findByPatientId(patientId);
    }

    public List<EmergencyRequest> getHospitalEmergencies(String hospitalId) {
        List<EmergencyRequest> list = new ArrayList<>(emergencyRepository.findByRecommendedHospitalId(hospitalId));
        hospitalRepository.findByUserId(hospitalId).ifPresent(h -> {
            if (!h.getId().equals(hospitalId)) {
                for (EmergencyRequest em : emergencyRepository.findByRecommendedHospitalId(h.getId())) {
                    if (!list.contains(em)) list.add(em);
                }
            }
        });
        return list;
    }

    public List<EmergencyRequest> getDriverEmergencies(String driverId) {
        List<EmergencyRequest> list = new ArrayList<>(emergencyRepository.findByAssignedDriverId(driverId));
        driverRepository.findByUserId(driverId).ifPresent(d -> {
            if (!d.getId().equals(driverId)) {
                for (EmergencyRequest em : emergencyRepository.findByAssignedDriverId(d.getId())) {
                    if (!list.contains(em)) list.add(em);
                }
            }
            for (Ambulance amb : ambulanceRepository.findByDriverId(d.getId())) {
                for (EmergencyRequest em : emergencyRepository.findAll()) {
                    if (amb.getId().equals(em.getAssignedAmbulanceId()) && !list.contains(em)) {
                        list.add(em);
                    }
                }
            }
        });

        for (Ambulance amb : ambulanceRepository.findByDriverId(driverId)) {
            for (EmergencyRequest em : emergencyRepository.findAll()) {
                if (amb.getId().equals(em.getAssignedAmbulanceId()) && !list.contains(em)) {
                    list.add(em);
                }
            }
        }

        return list;
    }
}
