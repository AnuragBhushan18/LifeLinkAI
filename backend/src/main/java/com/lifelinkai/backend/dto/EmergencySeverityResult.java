package com.lifelinkai.backend.dto;

import com.lifelinkai.backend.model.EmergencySeverity;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EmergencySeverityResult {
    private EmergencySeverity severity;
    private int score;
    private List<String> severityFactors;
}
