package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.dtos.CustomUserDto;
import com.user_management_service.user_management_service.exceptions.InvalidOtpException;
import com.user_management_service.user_management_service.exceptions.OtpLimitExceededException;
import com.user_management_service.user_management_service.models.Otp;
import com.user_management_service.user_management_service.repositories.OtpRepository;
import com.user_management_service.user_management_service.services.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    @InjectMocks
    private OtpService otpService;

    @Captor
    private ArgumentCaptor<Otp> otpCaptor;

    @Captor
    private ArgumentCaptor<CustomUserDto> customUserDtoCaptor;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_OTP = "123456";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 5);
        ReflectionTestUtils.setField(otpService, "maxOtpAttempts", 3);
        ReflectionTestUtils.setField(otpService, "otpAttemptWindowHours", 24);
    }

    @Test
    void generateAndSendOtp_Success() {
        // Arrange
        when(otpRepository.countByEmailAndCreatedAtAfter(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(otpRepository.save(any(Otp.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        otpService.generateAndSendOtp(TEST_EMAIL, TEST_NAME);

        // Assert
        verify(otpRepository).save(otpCaptor.capture());
        verify(kafkaTemplate).send(eq("otp-request"), customUserDtoCaptor.capture());

        Otp savedOtp = otpCaptor.getValue();
        assertEquals(TEST_EMAIL, savedOtp.getEmail());
        assertNotNull(savedOtp.getOtp());
        assertEquals(6, savedOtp.getOtp().length());
        assertTrue(savedOtp.getExpiryTime().isAfter(LocalDateTime.now()));

        CustomUserDto sentDto = customUserDtoCaptor.getValue();
        assertEquals(TEST_EMAIL, sentDto.getEmail());
        assertEquals(TEST_NAME, sentDto.getName());
        assertEquals(savedOtp.getOtp(), sentDto.getPassword());
    }

    @Test
    void generateAndSendOtp_ExceedsMaxAttempts_ThrowsException() {
        // Arrange
        when(otpRepository.countByEmailAndCreatedAtAfter(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(3L);

        // Act & Assert
        assertThrows(OtpLimitExceededException.class,
                () -> otpService.generateAndSendOtp(TEST_EMAIL, TEST_NAME));

        verify(otpRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any(CustomUserDto.class));
    }

    @Test
    void generateAndSendOtp_WithInvalidEmail_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> otpService.generateAndSendOtp(null, TEST_NAME));
        assertThrows(IllegalArgumentException.class,
                () -> otpService.generateAndSendOtp("", TEST_NAME));
        assertThrows(IllegalArgumentException.class,
                () -> otpService.generateAndSendOtp("   ", TEST_NAME));

        verify(otpRepository, never()).countByEmailAndCreatedAtAfter(any(), any());
        verify(otpRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any(CustomUserDto.class));
    }

    @Test
    void verifyOtp_Success() {
        // Arrange
        Otp validOtp = new Otp();
        validOtp.setEmail(TEST_EMAIL);
        validOtp.setOtp(TEST_OTP);
        validOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmailAndOtpAndExpiryTimeAfter(
                eq(TEST_EMAIL),
                eq(TEST_OTP),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(validOtp));

        // Act
        boolean result = otpService.verifyOtp(TEST_EMAIL, TEST_OTP);

        // Assert
        assertTrue(result);
        verify(otpRepository).delete(validOtp);
    }

    @Test
    void verifyOtp_WithInvalidOtp_ThrowsException() {
        // Arrange
        when(otpRepository.findByEmailAndOtpAndExpiryTimeAfter(
                eq(TEST_EMAIL),
                eq(TEST_OTP),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidOtpException.class,
                () -> otpService.verifyOtp(TEST_EMAIL, TEST_OTP));

        verify(otpRepository, never()).delete(any());
    }

    @Test
    void verifyOtp_WithInvalidEmail_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp(null, TEST_OTP));
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("", TEST_OTP));
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("   ", TEST_OTP));

        verify(otpRepository, never()).findByEmailAndOtpAndExpiryTimeAfter(any(), any(), any());
        verify(otpRepository, never()).delete(any());
    }

    @Test
    void verifyOtp_WithEmptyOtp_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp(TEST_EMAIL, null));
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp(TEST_EMAIL, ""));
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp(TEST_EMAIL, "   "));

        verify(otpRepository, never()).findByEmailAndOtpAndExpiryTimeAfter(any(), any(), any());
        verify(otpRepository, never()).delete(any());
    }

    @Test
    void generateSecureOtp_ReturnsValidOtp() {
        // Arrange
        when(otpRepository.countByEmailAndCreatedAtAfter(eq(TEST_EMAIL), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(otpRepository.save(any(Otp.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        otpService.generateAndSendOtp(TEST_EMAIL, TEST_NAME);

        // Assert
        verify(otpRepository).save(otpCaptor.capture());
        String generatedOtp = otpCaptor.getValue().getOtp();

        assertNotNull(generatedOtp);
        assertEquals(6, generatedOtp.length());
        assertTrue(generatedOtp.matches("\\d{6}"));
    }
}