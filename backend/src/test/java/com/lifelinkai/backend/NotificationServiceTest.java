package com.lifelinkai.backend;

import com.lifelinkai.backend.dto.NotificationDto;
import com.lifelinkai.backend.model.Notification;
import com.lifelinkai.backend.model.NotificationType;
import com.lifelinkai.backend.repository.NotificationRepository;
import com.lifelinkai.backend.service.NotificationService;
import com.lifelinkai.backend.websocket.RealtimeEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private RealtimeEventPublisher realtimeEventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id("notif-1")
                .recipientId("usr-1")
                .type(NotificationType.AMBULANCE_ASSIGNED)
                .title("Ambulance Assigned")
                .message("Ambulance #101 has been dispatched")
                .relatedEmergencyId("em-1")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateNotification_PersistsAndPublishes() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId("generated-id");
            return n;
        });

        NotificationDto dto = notificationService.createNotification(
                "usr-1",
                NotificationType.AMBULANCE_ASSIGNED,
                "Ambulance Assigned",
                "Ambulance #101 has been dispatched",
                "em-1"
        );

        assertNotNull(dto);
        assertEquals("generated-id", dto.getId());
        assertEquals("usr-1", dto.getRecipientId());
        assertEquals(NotificationType.AMBULANCE_ASSIGNED, dto.getType());
        assertFalse(dto.isRead());

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(realtimeEventPublisher, times(1)).publishNotification(eq("usr-1"), any(NotificationDto.class));
    }

    @Test
    void testGetUserNotifications() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc("usr-1"))
                .thenReturn(List.of(sampleNotification));

        List<NotificationDto> result = notificationService.getUserNotifications("usr-1");
        assertEquals(1, result.size());
        assertEquals("notif-1", result.get(0).getId());
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationDto updated = notificationService.markAsRead("notif-1", "usr-1");
        assertTrue(updated.isRead());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    void testMarkAsRead_UnauthorizedUser_ThrowsException() {
        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(sampleNotification));

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead("notif-1", "other-user"));
    }

    @Test
    void testMarkAllAsRead() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc("usr-1"))
                .thenReturn(List.of(sampleNotification));

        notificationService.markAllAsRead("usr-1");
        assertTrue(sampleNotification.isRead());
        verify(notificationRepository, times(1)).saveAll(any());
    }
}
