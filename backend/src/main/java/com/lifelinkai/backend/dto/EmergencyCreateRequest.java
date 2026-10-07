package com.lifelinkai.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class EmergencyCreateRequest {
    private double latitude;
    private double longitude;
    private List<String> symptoms;
    private String emergencyDescription;
}
