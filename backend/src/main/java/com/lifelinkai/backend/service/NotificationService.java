package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.NotificationDto;
import com.lifelinkai.backend.model.Notification;
import com.lifelinkai.backend.model.NotificationType;
import com.lifelinkai.backend.repository.NotificationRepository;
import com.lifelinkai.backend.websocket.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public NotificationDto createNotification(String recipientId, NotificationType type, String title, String message, String relatedEmergencyId) {
        if (recipientId == null) {
            log.warn("Cannot create notification with null recipientId");
            return null;
        }

        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .type(type)
                .title(title)
                .message(message)
                .relatedEmergencyId(relatedEmergencyId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);
        realtimeEventPublisher.publishNotification(recipientId, dto);
        return dto;
    }

    public List<NotificationDto> getUserNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    public NotificationDto markAsRead(String id, String recipientId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new RuntimeException("Unauthorized: notification does not belong to this user");
        }

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return toDto(notification);
    }

    public void markAllAsRead(String recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .filter(n -> !n.isRead())
                .peek(n -> n.setRead(true))
                .collect(Collectors.toList());

        if (!unread.isEmpty()) {
            notificationRepository.saveAll(unread);
        }
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedEmergencyId(n.getRelatedEmergencyId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
