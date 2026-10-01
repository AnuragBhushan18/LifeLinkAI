package com.lifelinkai.backend;

import com.lifelinkai.backend.model.EmergencyStatus;
import com.lifelinkai.backend.service.EmergencyStateService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmergencyStateServiceTest {

    private final EmergencyStateService service = new EmergencyStateService();

    @Test
    void testValidTransitions() {
        assertTrue(service.isValidTransition(EmergencyStatus.CREATED, EmergencyStatus.SEVERITY_ANALYZED));
        assertTrue(service.isValidTransition(EmergencyStatus.SEVERITY_ANALYZED, EmergencyStatus.AMBULANCE_SEARCHING));
        assertTrue(service.isValidTransition(EmergencyStatus.AMBULANCE_ASSIGNED, EmergencyStatus.GOING_TO_PATIENT));
        assertTrue(service.isValidTransition(EmergencyStatus.ADMITTED, EmergencyStatus.TREATMENT_IN_PROGRESS));
    }

    @Test
    void testInvalidTransitions() {
        assertFalse(service.isValidTransition(EmergencyStatus.COMPLETED, EmergencyStatus.GOING_TO_PATIENT));
        assertFalse(service.isValidTransition(EmergencyStatus.CREATED, EmergencyStatus.ARRIVED_AT_HOSPITAL));
        assertFalse(service.isValidTransition(EmergencyStatus.CANCELLED, EmergencyStatus.CREATED));
    }
}
