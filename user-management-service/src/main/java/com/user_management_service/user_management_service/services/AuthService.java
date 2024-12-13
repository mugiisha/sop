package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.repositories.AuthRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopService.GetRoleByUserIdResponse;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuditService auditService;
    private final UserRoleClientService userRoleClientService;
    private final KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    private static final int MAX_LOGIN_ATTEMPTS = 10;

    public LoginResponseDTO login(UserLoginDTO loginDTO) {
        log.info("Attempting login for user: {}", loginDTO.getEmail());

        User user = authRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login attempt for non-existent user: {}", loginDTO.getEmail());
                    return new AuthenticationException("Invalid credentials");
                });

        GetRoleByUserIdResponse userRole = userRoleClientService.getUserRoles(user.getId().toString());
        String roleName = userRole.getRoleName();

        validateUserStatus(user);

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password attempt for user: {}", loginDTO.getEmail());
            handleFailedLoginAttempt(user);
            throw new AuthenticationException("Invalid credentials");
        }

        handleSuccessfulLoginAttempt(user);
        String token = jwtService.generateToken(user, roleName);

        log.info("Successfully logged in user: {}", loginDTO.getEmail());
        return createLoginResponse(user, token, roleName);
    }

    @Transactional
    public OtpResponseDTO requestPasswordReset(@Valid EmailRequestDTO requestDTO) {
        log.info("Password reset requested for email: {}", requestDTO.getEmail());

        User user = authRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("Password reset requested for non-existent user: {}", requestDTO.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        if (!user.isActive()) {
            log.warn("Password reset attempted for inactive account: {}", requestDTO.getEmail());
            throw new IllegalStateException("Cannot reset password for inactive account");
        }

        otpService.generateAndSendOtp(requestDTO.getEmail(), user.getName());
        auditService.logPasswordResetRequest(user.getId(), user.getEmail());
        log.info("Password reset OTP sent for user: {}", requestDTO.getEmail());
        return null;
    }

    public String verifyOtpAndGenerateToken(OtpVerificationDTO verificationDTO) {
        log.info("Verifying OTP for email: {}", verificationDTO.getEmail());

        User user = authRepository.findByEmail(verificationDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("OTP verification attempted for non-existent user: {}", verificationDTO.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        otpService.verifyOtp(verificationDTO.getEmail(), verificationDTO.getOtp());
        String token = jwtService.generatePasswordResetToken(user);

        log.info("OTP verified successfully for user: {}", verificationDTO.getEmail());
        return token;
    }

    @Transactional
    public void resetPassword(String resetToken, PasswordResetDTO resetDTO) {
        validatePasswordReset(resetDTO);

        String email = jwtService.validatePasswordResetTokenAndGetEmail(resetToken);
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateNewPassword(resetDTO.getNewPassword(), user);
        updateUserPassword(user, resetDTO.getNewPassword());

        log.info("Password reset successful for user: {}", email);
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Processing email verification for token: {}", token);

        User user = authRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid email verification token attempted: {}", token);
                    return new InvalidTokenException("Invalid verification token");
                });

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Expired email verification token attempted for user: {}", user.getEmail());
            throw new InvalidTokenException("Verification token has expired");
        }

        completeEmailVerification(user);
        log.info("Email verification successful for user: {}", user.getEmail());
    }

    public void handleFailedLoginAttempt(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        user.setLastFailedLogin(LocalDateTime.now());

        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            lockUserAccount(user);
        }

        authRepository.save(user);
    }

    public void handleSuccessfulLoginAttempt(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastFailedLogin(null);
        user.setLastLogin(LocalDateTime.now());
        authRepository.save(user);
        auditService.logUserLogin(user.getId(), user.getEmail());
    }

    private void validateUserStatus(User user) {
        if (!user.isActive()) {
            log.warn("Inactive user attempted to login: {}", user.getEmail());
            throw new AuthenticationException("Account is disabled");
        }

        if (!user.isEmailVerified()) {
            log.warn("Unverified user attempted to login: {}", user.getEmail());
            throw new AuthenticationException("Email not verified");
        }
    }

    private void lockUserAccount(User user) {
        user.setActive(false);
        user.setDeactivatedAt(LocalDateTime.now());

        // prepare object to be sent via kafka
        CustomUserDto customUserDto = new CustomUserDto();
        customUserDto.setId(user.getId());
        customUserDto.setEmail(user.getEmail());
        customUserDto.setName(user.getName());

        kafkaTemplate.send("user-locked", customUserDto);
        auditService.logAccountLocked(user.getId(), user.getEmail());
        log.warn("Account locked due to multiple failed attempts: {}", user.getEmail());
    }

    private void validatePasswordReset(PasswordResetDTO resetDTO) {
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }
    }

    private void validateNewPassword(String newPassword, User user) {
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ValidationException("New password must be different from current password");
        }
    }

    private void updateUserPassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLastFailedLogin(null);
        if (!user.isActive()) {
            user.setActive(true);
            user.setDeactivatedAt(null);
        }
        authRepository.save(user);

        // prepare object to send via kafka
        CustomUserDto createdUserDto = new CustomUserDto();
        createdUserDto.setId(user.getId());
        createdUserDto.setEmail(user.getEmail());
        createdUserDto.setName(user.getName());

        kafkaTemplate.send("password-updated", createdUserDto);
        auditService.logPasswordReset(user.getId(), user.getEmail());
    }

    private void completeEmailVerification(User user) {
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        authRepository.save(user);

        auditService.logEmailVerification(user.getId(), user.getEmail());
    }

    private LoginResponseDTO createLoginResponse(User user, String token, String roleName) {
        UserDTO userDTO = new UserDTO(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                user.getLastLogin().toString()
        );

        return new LoginResponseDTO(
                roleName,
                token,
                userDTO
        );
    }
}