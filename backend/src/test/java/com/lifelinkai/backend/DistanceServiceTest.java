package com.lifelinkai.backend;

import com.lifelinkai.backend.service.DistanceService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DistanceServiceTest {

    private final DistanceService service = new DistanceService();

    @Test
    void testCalculateDistanceKm() {
        // Distance between two coords
        double lat1 = 28.6139; // Delhi
        double lon1 = 77.2090;
        double lat2 = 19.0760; // Mumbai
        double lon2 = 72.8777;
        
        double distance = service.calculateDistanceKm(lat1, lon1, lat2, lon2);
        assertTrue(distance > 1000 && distance < 1200);
    }
}
