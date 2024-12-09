package com.notification_service.notification_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.notification_service.notification_service.dtos.CustomUserDto;
import com.notification_service.notification_service.utils.DtoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaListenerServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationPreferencesService notificationPreferencesService;

    @InjectMocks
    private KafkaListenerService kafkaListenerService;

    private CustomUserDto customUserDto;

    @BeforeEach
    void setUp() {
        customUserDto = new CustomUserDto();
        customUserDto.setId(UUID.randomUUID());
        customUserDto.setEmail("test@example.com");
        customUserDto.setName("Test User");
        customUserDto.setVerificationToken("verification-token");
        customUserDto.setPassword("password");
    }

    @Test
    void userCreatedListener() throws JsonProcessingException {
        // Given
        String data = DtoConverter.userDtoTojson(customUserDto);

        // When
        kafkaListenerService.userCreatedListener(data);

        // Then
        verify(notificationPreferencesService).createNotificationPreferences(customUserDto.getId(), customUserDto.getEmail());
        verify(emailService, times(1)).sendVerificationEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getVerificationToken());
        verify(emailService,times(1)).sendWelcomeEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getPassword());
    }

    @Test
    void userDeactivatedListener() throws JsonProcessingException {
        // Given
        String data = DtoConverter.userDtoTojson(customUserDto);

        // When
        kafkaListenerService.userDeactivatedListener(data);

        // Then
        verify(emailService,times(1)).sendAccountDeactivationEmail(customUserDto.getEmail(), customUserDto.getName());
    }

    @Test
    void resendVerificationListener() throws JsonProcessingException {
        // Given
        String data = DtoConverter.userDtoTojson(customUserDto);

        // When
        kafkaListenerService.resendVerificationListener(data);

        // Then
        verify(emailService,times(1)).sendVerificationEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getVerificationToken());
    }

}