package com.lifelinkai.backend.service;

import com.lifelinkai.backend.model.EmergencyStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmergencyStateService {

    private static final Map<EmergencyStatus, EnumSet<EmergencyStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        // Patient creation
        VALID_TRANSITIONS.put(EmergencyStatus.CREATED, EnumSet.of(EmergencyStatus.SEVERITY_ANALYZED, EmergencyStatus.CANCELLED));
        
        // System processing
        VALID_TRANSITIONS.put(EmergencyStatus.SEVERITY_ANALYZED, EnumSet.of(EmergencyStatus.AMBULANCE_SEARCHING, EmergencyStatus.CANCELLED));
        VALID_TRANSITIONS.put(EmergencyStatus.AMBULANCE_SEARCHING, EnumSet.of(EmergencyStatus.AMBULANCE_ASSIGNED, EmergencyStatus.CANCELLED));
        VALID_TRANSITIONS.put(EmergencyStatus.AMBULANCE_ASSIGNED, EnumSet.of(EmergencyStatus.GOING_TO_PATIENT, EmergencyStatus.CANCELLED));
        
        // Driver actions
        VALID_TRANSITIONS.put(EmergencyStatus.GOING_TO_PATIENT, EnumSet.of(EmergencyStatus.ARRIVED_AT_PICKUP, EmergencyStatus.CANCELLED));
        VALID_TRANSITIONS.put(EmergencyStatus.ARRIVED_AT_PICKUP, EnumSet.of(EmergencyStatus.PATIENT_PICKED_UP, EmergencyStatus.CANCELLED));
        VALID_TRANSITIONS.put(EmergencyStatus.PATIENT_PICKED_UP, EnumSet.of(EmergencyStatus.GOING_TO_HOSPITAL));
        VALID_TRANSITIONS.put(EmergencyStatus.GOING_TO_HOSPITAL, EnumSet.of(EmergencyStatus.ARRIVED_AT_HOSPITAL));
        VALID_TRANSITIONS.put(EmergencyStatus.ARRIVED_AT_HOSPITAL, EnumSet.of(EmergencyStatus.BED_RESERVED, EmergencyStatus.ADMITTED));
        
        // Hospital actions
        VALID_TRANSITIONS.put(EmergencyStatus.BED_RESERVED, EnumSet.of(EmergencyStatus.ADMITTED));
        VALID_TRANSITIONS.put(EmergencyStatus.ADMITTED, EnumSet.of(EmergencyStatus.TREATMENT_IN_PROGRESS));
        VALID_TRANSITIONS.put(EmergencyStatus.TREATMENT_IN_PROGRESS, EnumSet.of(EmergencyStatus.COMPLETED));
        
        // Terminal states
        VALID_TRANSITIONS.put(EmergencyStatus.COMPLETED, EnumSet.noneOf(EmergencyStatus.class));
        VALID_TRANSITIONS.put(EmergencyStatus.CANCELLED, EnumSet.noneOf(EmergencyStatus.class));
        VALID_TRANSITIONS.put(EmergencyStatus.REJECTED, EnumSet.noneOf(EmergencyStatus.class));
    }

    public boolean isValidTransition(EmergencyStatus currentState, EmergencyStatus nextState) {
        if (currentState == null || nextState == null) {
            return false;
        }
        EnumSet<EmergencyStatus> validNextStates = VALID_TRANSITIONS.get(currentState);
        return validNextStates != null && validNextStates.contains(nextState);
    }
}
