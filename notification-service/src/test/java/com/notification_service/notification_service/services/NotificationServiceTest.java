package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.*;
import com.notification_service.notification_service.enums.NotificationType;
import com.notification_service.notification_service.models.Notification;
import com.notification_service.notification_service.models.NotificationPreferences;
import com.notification_service.notification_service.repositories.NotificationRepository;
import com.notification_service.notification_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationPreferencesService notificationPreferencesService;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private CreateNotificationDto createNotificationDto;
    private Notification notification;
    private NotificationPreferences preferences;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        createNotificationDto = new CreateNotificationDto();
        createNotificationDto.setUserId(userId);
        createNotificationDto.setMessage("Test notification");
        createNotificationDto.setType(NotificationType.SOP_ASSIGNED_AS_AUTHOR);
        createNotificationDto.setSopId("123");
        createNotificationDto.setSopTitle("Test SOP");

        notification = new Notification();
        notification.setId("1");
        notification.setUserId(userId);
        notification.setMessage("Test notification");
        notification.setType(NotificationType.SOP_ASSIGNED_AS_AUTHOR);
        notification.setSopId("123");
        notification.setSopTitle("Test SOP");
        notification.setCreatedAt(new Date());

        preferences = new NotificationPreferences();
        preferences.setUserId(userId);
        preferences.setEmail("test@example.com");
        preferences.setAllSOPAlertsEnabled(true);
        preferences.setAuthorAlertsEnabled(true);
        preferences.setReviewerAlertsEnabled(true);
        preferences.setApproverAlertsEnabled(true);
    }

    @Test
    void createAndSendNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(), anyString(), any(Notification.class));

        Notification result = notificationService.createAndSendNotification(createNotificationDto);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(createNotificationDto.getMessage(), result.getMessage());
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                userId.toString(), "/queue/notifications", result);
    }

    @Test
    void getNotificationsByUserId_Success() {
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(notifications);

        List<Notification> result = notificationService.getNotificationsByUserId(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(notification, result.get(0));
    }

    @Test
    void getNotificationById_Success() throws NotFoundException {
        when(notificationRepository.findById("1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.getNotificationById("1");

        assertNotNull(result);
        assertTrue(result.isRead());
        assertEquals("1", result.getId());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotificationById_NotFound() {
        when(notificationRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                notificationService.getNotificationById("1")
        );
    }

    @Test
    void sopCreatedListener_Success() {
        SOPDto sopDto = new SOPDto();
        sopDto.setId("123");
        sopDto.setTitle("Test SOP");
        sopDto.setAuthorId(userId);
        sopDto.setApproverId(UUID.randomUUID());
        sopDto.setReviewers(Collections.singletonList(UUID.randomUUID()));

        when(notificationPreferencesService.getUserNotificationPreferences(any(UUID.class)))
                .thenReturn(preferences);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        String sopData = "{\"id\":\"123\",\"title\":\"Test SOP\",\"authorId\":\"" + userId + "\"}";
        notificationService.sopCreatedListener(sopData);

        verify(notificationPreferencesService, times(3))
                .getUserNotificationPreferences(any(UUID.class));
        verify(notificationRepository, times(3)).save(any(Notification.class));
        verify(emailService, times(3))
                .sendSOPWorkflowEmail(anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void userCreatedListener_Success() {
        String userData = "{\"id\":\"" + userId + "\",\"email\":\"test@example.com\"," +
                "\"name\":\"Test User\",\"verificationToken\":\"token123\"}";

        doNothing().when(notificationPreferencesService)
                .createNotificationPreferences(any(UUID.class), anyString());
        doNothing().when(emailService)
                .sendVerificationEmail(anyString(), anyString(), anyString());

        notificationService.userCreatedListener(userData);

        verify(notificationPreferencesService)
                .createNotificationPreferences(any(UUID.class), anyString());
        verify(emailService)
                .sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sopReviewedListener_Success() {
        SOPDto sopDto = new SOPDto();
        sopDto.setId("123");
        sopDto.setTitle("Test SOP");
        sopDto.setApproverId(userId);

        when(notificationPreferencesService.getUserNotificationPreferences(userId))
                .thenReturn(preferences);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        String sopData = "{\"id\":\"123\",\"title\":\"Test SOP\",\"approverId\":\"" + userId + "\"}";
        notificationService.sopReviewedListener(sopData);

        verify(notificationPreferencesService).getUserNotificationPreferences(userId);
        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendSOPWorkflowEmail(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sopApprovedListener_Success() {
        SOPDto sopDto = new SOPDto();
        sopDto.setId("123");
        sopDto.setTitle("Test SOP");
        sopDto.setAuthorId(userId);
        sopDto.setApproverId(UUID.randomUUID());
        sopDto.setReviewers(Collections.singletonList(UUID.randomUUID()));

        when(notificationPreferencesService.getUserNotificationPreferences(any(UUID.class)))
                .thenReturn(preferences);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        String sopData = "{\"id\":\"123\",\"title\":\"Test SOP\",\"authorId\":\"" + userId + "\"}";
        notificationService.sopApprovedListener(sopData);

        verify(notificationPreferencesService, times(3))
                .getUserNotificationPreferences(any(UUID.class));
        verify(notificationRepository, times(3)).save(any(Notification.class));
        verify(emailService, times(3))
                .sendSOPWorkflowEmail(anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString());
    }
}