package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.UpdateNotificationPreferencesDto;
import com.notification_service.notification_service.models.NotificationPreferences;
import com.notification_service.notification_service.repositories.NotificationPreferencesRepository;
import com.notification_service.notification_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    private NotificationPreferencesService notificationPreferencesService;

    @Mock
    private NotificationPreferencesRepository notificationPreferencesRepository;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        notificationPreferencesService = new NotificationPreferencesService(notificationPreferencesRepository);
    }

    @Nested
    class CreateNotificationPreferences {
        @Test
        void shouldSuccessfullyCreateNotificationPreferences() {
            // When
            notificationPreferencesService.createNotificationPreferences(TEST_USER_ID, TEST_EMAIL);

            // Then
            ArgumentCaptor<NotificationPreferences> notificationPreferencesArgumentCaptor = ArgumentCaptor.forClass(NotificationPreferences.class);
            verify(notificationPreferencesRepository).save(notificationPreferencesArgumentCaptor.capture());

            NotificationPreferences capturedNotificationPreferences = notificationPreferencesArgumentCaptor.getValue();
            assertThat(capturedNotificationPreferences.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(capturedNotificationPreferences.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(capturedNotificationPreferences.isEmailEnabled()).isTrue();
            assertThat(capturedNotificationPreferences.isInAppEnabled()).isTrue();
        }
    }

    @Nested
    class UpdateNotificationPreferences {
        @Test
        void shouldSuccessfullyUpdateNotificationPreferences() {
            // Given
            UpdateNotificationPreferencesDto updateDto = new UpdateNotificationPreferencesDto(false, true);
            NotificationPreferences existingPreferences = createTestNotificationPreferences();
            given(notificationPreferencesRepository.findByUserId(TEST_USER_ID)).willReturn(existingPreferences);
            given(notificationPreferencesRepository.save(any(NotificationPreferences.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            NotificationPreferences updatedPreferences = notificationPreferencesService.updateNotificationPreferences(TEST_USER_ID, updateDto);

            // Then
            assertThat(updatedPreferences.isEmailEnabled()).isEqualTo(updateDto.isEmailEnabled());
            assertThat(updatedPreferences.isInAppEnabled()).isEqualTo(updateDto.isInAppEnabled());
        }

        @Test
        void shouldThrowExceptionWhenNotificationPreferencesNotFound() {
            // Given
            UpdateNotificationPreferencesDto updateDto = new UpdateNotificationPreferencesDto(false, true);
            given(notificationPreferencesRepository.findByUserId(TEST_USER_ID)).willReturn(null);

            // When/Then
            assertThatThrownBy(() -> notificationPreferencesService.updateNotificationPreferences(TEST_USER_ID, updateDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Notification Preference not found for user id: " + TEST_USER_ID);
        }
    }

    @Nested
    class GetUserNotificationPreferences {
        @Test
        void shouldSuccessfullyGetUserNotificationPreferences() {
            // Given
            NotificationPreferences existingPreferences = createTestNotificationPreferences();
            given(notificationPreferencesRepository.findByUserId(TEST_USER_ID)).willReturn(existingPreferences);

            // When
            NotificationPreferences preferences = notificationPreferencesService.getUserNotificationPreferences(TEST_USER_ID);

            // Then
            assertThat(preferences).isEqualTo(existingPreferences);
        }

        @Test
        void shouldThrowExceptionWhenNotificationPreferencesNotFound() {
            // Given
            given(notificationPreferencesRepository.findByUserId(TEST_USER_ID)).willReturn(null);

            // When/Then
            assertThatThrownBy(() -> notificationPreferencesService.getUserNotificationPreferences(TEST_USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Notification Preference not found for user id: " + TEST_USER_ID);
        }
    }

    @Nested
    class DeleteNotificationPreferences {
        @Test
        void shouldSuccessfullyDeleteNotificationPreferences() {
            // Given
            NotificationPreferences existingPreferences = createTestNotificationPreferences();
            given(notificationPreferencesRepository.findByEmail(TEST_EMAIL)).willReturn(existingPreferences);

            // When
            notificationPreferencesService.deleteNotificationPreferences(TEST_EMAIL);

            // Then
            verify(notificationPreferencesRepository).delete(existingPreferences);
        }

        @Test
        void shouldThrowExceptionWhenNotificationPreferencesNotFound() {
            // Given
            given(notificationPreferencesRepository.findByEmail(TEST_EMAIL)).willReturn(null);

            // When/Then
            assertThatThrownBy(() -> notificationPreferencesService.deleteNotificationPreferences(TEST_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Notification Preference not found for user id: " + TEST_EMAIL);
        }
    }

    // Helper methods
    private NotificationPreferences createTestNotificationPreferences() {
        NotificationPreferences preferences = new NotificationPreferences();
        preferences.setUserId(TEST_USER_ID);
        preferences.setEmail(TEST_EMAIL);
        preferences.setEmailEnabled(true);
        preferences.setInAppEnabled(true);
        return preferences;
    }
}