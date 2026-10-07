package com.lifelinkai.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyTimelineEvent {
    private String id;
    private EmergencyStatus status;
    private String title;
    private String description;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
