package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.repositories.AuthRepository;
import com.user_management_service.user_management_service.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import sopService.GetRoleByUserIdResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private OtpService otpService;
    @Mock
    private AuditService auditService;
    @Mock
    private UserRoleClientService userRoleClientService;
    @Mock
    private KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserLoginDTO loginDTO;
    private String testEmail;
    private UUID userId;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(testEmail);
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedPassword");
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setLastLogin(LocalDateTime.now());
        testUser.setFailedLoginAttempts(0);

        loginDTO = new UserLoginDTO(testEmail, "password123");

        // Use lenient() for stubs that might not be used in every test
        CompletableFuture<SendResult<String, CustomUserDto>> future = new CompletableFuture<>();
        lenient().when(kafkaTemplate.send(anyString(), any(CustomUserDto.class)))
                .thenReturn(future);
    }

    @Test
    void login_WhenCredentialsValid_ShouldReturnLoginResponse() {
        // Arrange
        GetRoleByUserIdResponse roleResponse = mock(GetRoleByUserIdResponse.class);
        when(roleResponse.getRoleName()).thenReturn("USER");

        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRoleClientService.getUserRoles(anyString())).thenReturn(roleResponse);
        when(jwtService.generateToken(any(User.class), anyString())).thenReturn("jwt-token");

        // Act
        LoginResponseDTO response = authService.login(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("USER", response.getRole());
        assertEquals("jwt-token", response.getToken());
        verify(auditService).logUserLogin(userId, testEmail);
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowAuthenticationException() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.login(loginDTO));
    }

    @Test
    void login_WhenUserInactive_ShouldThrowAuthenticationException() {
        testUser.setActive(false);
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> authService.login(loginDTO));
    }

    @Test
    void login_WhenPasswordInvalid_ShouldIncrementFailedAttempts() {
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.login(loginDTO));
        verify(authRepository).save(argThat(user -> user.getFailedLoginAttempts() == 1));
    }

    @Test
    void login_WhenMaxAttemptsExceeded_ShouldLockAccount() {
        testUser.setFailedLoginAttempts(9); // One more attempt will exceed the limit

        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.login(loginDTO));

        verify(kafkaTemplate).send(eq("user-locked"), any(CustomUserDto.class));
        verify(auditService).logAccountLocked(userId, testEmail);
        verify(authRepository).save(argThat(user ->
                !user.isActive() && user.getDeactivatedAt() != null));
    }

    @Test
    void requestPasswordReset_WhenValidEmail_ShouldGenerateOtp() {
        EmailRequestDTO requestDTO = new EmailRequestDTO();
        requestDTO.setEmail(testEmail);

        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        doNothing().when(otpService).generateAndSendOtp(anyString(), anyString());

        authService.requestPasswordReset(requestDTO);

        verify(otpService).generateAndSendOtp(testEmail, testUser.getName());
        verify(auditService).logPasswordResetRequest(userId, testEmail);
    }

    @Test
    void requestPasswordReset_WhenUserInactive_ShouldThrowIllegalStateException() {
        EmailRequestDTO requestDTO = new EmailRequestDTO();
        requestDTO.setEmail(testEmail);
        testUser.setActive(false);

        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalStateException.class,
                () -> authService.requestPasswordReset(requestDTO));
    }

    @Test
    void verifyEmail_WhenValidToken_ShouldVerifyEmail() {
        String token = "valid-token";
        testUser.setEmailVerified(false);
        testUser.setEmailVerificationToken(token);
        testUser.setEmailVerificationTokenExpiry(LocalDateTime.now().plusDays(1));

        when(authRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));

        authService.verifyEmail(token);

        verify(authRepository).save(argThat(user ->
                user.isEmailVerified() &&
                        user.getEmailVerificationToken() == null &&
                        user.getEmailVerificationTokenExpiry() == null));
        verify(auditService).logEmailVerification(userId, testEmail);
    }

    @Test
    void resetPassword_WhenValidRequest_ShouldUpdatePassword() {
        String resetToken = "valid-reset-token";
        PasswordResetDTO resetDTO = new PasswordResetDTO("Password123", "Password123");
        resetDTO.setNewPassword("Password123");
        resetDTO.setConfirmPassword("Password123");

        when(jwtService.validatePasswordResetTokenAndGetEmail(resetToken)).thenReturn(testEmail);
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");

        authService.resetPassword(resetToken, resetDTO);

        verify(kafkaTemplate).send(eq("password-updated"), any(CustomUserDto.class));
        verify(auditService).logPasswordReset(userId, testEmail);
        verify(authRepository).save(argThat(user ->
                user.getPasswordHash().equals("newHashedPassword") &&
                        user.getFailedLoginAttempts() == 0 &&
                        user.getLastFailedLogin() == null));
    }

    @Test
    void resetPassword_WhenPasswordsMismatch_ShouldThrowPasswordMismatchException() {
        String resetToken = "valid-reset-token";
        PasswordResetDTO resetDTO = new PasswordResetDTO("Password123", "DifferentPass123");
        resetDTO.setNewPassword("Password123");
        resetDTO.setConfirmPassword("DifferentPass123");

        assertThrows(PasswordMismatchException.class,
                () -> authService.resetPassword(resetToken, resetDTO));
    }

    @Test
    void resetPassword_WhenSameAsOldPassword_ShouldThrowValidationException() {
        String resetToken = "valid-reset-token";
        String currentPassword = "Password123";
        PasswordResetDTO resetDTO = new PasswordResetDTO(currentPassword, currentPassword);
        resetDTO.setNewPassword(currentPassword);
        resetDTO.setConfirmPassword(currentPassword);

        when(jwtService.validatePasswordResetTokenAndGetEmail(resetToken)).thenReturn(testEmail);
        when(authRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(resetDTO.getNewPassword(), testUser.getPasswordHash())).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> authService.resetPassword(resetToken, resetDTO));
    }
}