package com.lifelinkai.backend.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceService {

    private static final int EARTH_RADIUS_KM = 6371;
    private static final double AVERAGE_SPEED_KM_PER_HOUR = 40.0; // configurable

    /**
     * Calculates distance using the Haversine formula.
     * @return Distance in kilometers
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Estimates ETA in minutes based on distance and assumed average speed.
     */
    public double estimateEtaMinutes(double distanceKm) {
        // ETA = distance / speed
        // Convert hours to minutes
        return (distanceKm / AVERAGE_SPEED_KM_PER_HOUR) * 60.0;
    }
}
