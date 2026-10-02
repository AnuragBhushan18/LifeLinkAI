package com.lifelinkai.backend.dto;

import com.lifelinkai.backend.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String id;
    private String recipientId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedEmergencyId;
    private boolean read;
    private LocalDateTime createdAt;
}
