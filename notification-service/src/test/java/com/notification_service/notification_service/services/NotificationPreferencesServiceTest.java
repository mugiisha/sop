package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.UpdateNotificationPreferencesDto;
import com.notification_service.notification_service.models.NotificationPreferences;
import com.notification_service.notification_service.repositories.NotificationPreferencesRepository;
import com.notification_service.notification_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    @Mock
    private NotificationPreferencesRepository notificationPreferenceRepository;

    @InjectMocks
    private NotificationPreferencesService notificationPreferencesService;

    private UUID userId;
    private String email;
    private NotificationPreferences notificationPreferences;
    private UpdateNotificationPreferencesDto updateDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";

        notificationPreferences = new NotificationPreferences();
        notificationPreferences.setUserId(userId);
        notificationPreferences.setEmail(email);
        notificationPreferences.setAllSOPAlertsEnabled(true);
        notificationPreferences.setAuthorAlertsEnabled(true);
        notificationPreferences.setReviewerAlertsEnabled(true);
        notificationPreferences.setApproverAlertsEnabled(true);

        updateDto = new UpdateNotificationPreferencesDto();
        updateDto.setAllSOPAlertsEnabled(false);
        updateDto.setAuthorAlertsEnabled(false);
        updateDto.setReviewerAlertsEnabled(false);
        updateDto.setApproverAlertsEnabled(false);
    }

    @Test
    void createNotificationPreferences_ShouldCreateSuccessfully() {
        // Arrange
        when(notificationPreferenceRepository.save(any(NotificationPreferences.class)))
                .thenReturn(notificationPreferences);

        // Act
        notificationPreferencesService.createNotificationPreferences(userId, email);

        // Assert
        verify(notificationPreferenceRepository, times(1)).save(any(NotificationPreferences.class));
    }

    @Test
    void updateNotificationPreferences_WhenPreferencesExist_ShouldUpdateSuccessfully() {
        // Arrange
        when(notificationPreferenceRepository.findByUserId(userId))
                .thenReturn(notificationPreferences);
        when(notificationPreferenceRepository.save(any(NotificationPreferences.class)))
                .thenReturn(notificationPreferences);

        // Act
        NotificationPreferences result = notificationPreferencesService
                .updateNotificationPreferences(userId, updateDto);

        // Assert
        assertNotNull(result);
        verify(notificationPreferenceRepository, times(1)).findByUserId(userId);
        verify(notificationPreferenceRepository, times(1)).save(any(NotificationPreferences.class));
        assertEquals(updateDto.isAllSOPAlertsEnabled(), result.isAllSOPAlertsEnabled());
        assertEquals(updateDto.isAuthorAlertsEnabled(), result.isAuthorAlertsEnabled());
        assertEquals(updateDto.isReviewerAlertsEnabled(), result.isReviewerAlertsEnabled());
        assertEquals(updateDto.isApproverAlertsEnabled(), result.isApproverAlertsEnabled());
    }

    @Test
    void updateNotificationPreferences_WhenPreferencesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(notificationPreferenceRepository.findByUserId(userId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                notificationPreferencesService.updateNotificationPreferences(userId, updateDto));
        verify(notificationPreferenceRepository, times(1)).findByUserId(userId);
        verify(notificationPreferenceRepository, never()).save(any(NotificationPreferences.class));
    }

    @Test
    void getUserNotificationPreferences_WhenPreferencesExist_ShouldReturnPreferences() {
        // Arrange
        when(notificationPreferenceRepository.findByUserId(userId))
                .thenReturn(notificationPreferences);

        // Act
        NotificationPreferences result = notificationPreferencesService
                .getUserNotificationPreferences(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        verify(notificationPreferenceRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getUserNotificationPreferences_WhenPreferencesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(notificationPreferenceRepository.findByUserId(userId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                notificationPreferencesService.getUserNotificationPreferences(userId));
        verify(notificationPreferenceRepository, times(1)).findByUserId(userId);
    }

    @Test
    void deleteNotificationPreferences_WhenPreferencesExist_ShouldDeleteSuccessfully() {
        // Arrange
        when(notificationPreferenceRepository.findByEmail(email))
                .thenReturn(notificationPreferences);

        // Act
        notificationPreferencesService.deleteNotificationPreferences(email);

        // Assert
        verify(notificationPreferenceRepository, times(1)).findByEmail(email);
        verify(notificationPreferenceRepository, times(1)).delete(notificationPreferences);
    }

    @Test
    void deleteNotificationPreferences_WhenPreferencesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(notificationPreferenceRepository.findByEmail(email))
                .thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                notificationPreferencesService.deleteNotificationPreferences(email));
        verify(notificationPreferenceRepository, times(1)).findByEmail(email);
        verify(notificationPreferenceRepository, never()).delete(any(NotificationPreferences.class));
    }
}