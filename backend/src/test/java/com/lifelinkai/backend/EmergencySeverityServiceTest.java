package com.lifelinkai.backend;

import com.lifelinkai.backend.dto.EmergencySeverityResult;
import com.lifelinkai.backend.model.EmergencySeverity;
import com.lifelinkai.backend.service.EmergencySeverityService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class EmergencySeverityServiceTest {

    private final EmergencySeverityService service = new EmergencySeverityService();

    @Test
    void testCriticalSeverity() {
        EmergencySeverityResult result = service.analyzeSeverity(
                Arrays.asList("difficulty breathing", "chest pain"),
                "Patient is experiencing severe chest pain and difficulty breathing."
        );
        assertEquals(EmergencySeverity.CRITICAL, result.getSeverity());
        assertTrue(result.getScore() >= 60);
    }

    @Test
    void testHighSeverity() {
        EmergencySeverityResult result = service.analyzeSeverity(
                Arrays.asList("severe bleeding"),
                "Cut leg."
        );
        assertEquals(EmergencySeverity.HIGH, result.getSeverity());
    }

    @Test
    void testMediumSeverity() {
        EmergencySeverityResult result = service.analyzeSeverity(
                Arrays.asList("fever", "dizziness"),
                "Feeling hot and dizzy."
        );
        assertEquals(EmergencySeverity.MEDIUM, result.getSeverity());
    }

    @Test
    void testLowSeverity() {
        EmergencySeverityResult result = service.analyzeSeverity(
                Arrays.asList("mild pain"),
                "Ache."
        );
        assertEquals(EmergencySeverity.LOW, result.getSeverity());
    }
}
