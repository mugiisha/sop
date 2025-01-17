package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    public void logUserCreation(UUID userId, String email) {
        log.info("User created - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logUserLogin(UUID userId, String email) {
        log.info("User login - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logUserLogout(UUID userId, String email) {
        log.info("User logout - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logPasswordResetRequest(UUID userId, String email) {
        log.info("Password reset requested - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logPasswordReset(UUID userId, String email) {
        log.info("Password reset completed - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logUserUpdate(UUID userId, String email) {
        log.info("User updated - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logUserDeactivation(UUID userId, String email) {
        log.info("User deactivated - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logNotificationPreferenceUpdate(UUID userId, String email) {
        log.info("Notification preferences updated - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logEmailVerification(UUID userId, String email) {
        log.info("Email verified - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logAccountLocked(UUID userId, String email) {
        log.info("Account locked due to failed attempts - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logFailedLoginAttempt(UUID userId, String email, int attemptCount) {
        log.warn("Failed login attempt - UserID: {}, Email: {}, Attempt #: {}, Timestamp: {}",
                userId, email, attemptCount, LocalDateTime.now());
    }

    public void logPasswordChange(UUID userId, String email) {
        log.info("Password changed - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logRoleChange(UUID userId, String email, Role oldRole, Role newRole) {
        log.info("Role changed - UserID: {}, Email: {}, Old Role: {}, New Role: {}, Timestamp: {}",
                userId, email, oldRole, newRole, LocalDateTime.now());
    }

    public void logVerificationTokenGenerated(UUID userId, String email) {
        log.info("Verification token generated - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logVerificationEmailResent(UUID userId, String email) {
        log.info("Verification email resent - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logForcedEmailVerification(UUID userId, String email) {
        log.info("Forced email verification - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }

    public void logProfilePictureUpdate(UUID userId, String email) {
        log.info("Profile picture updated - UserID: {}, Email: {}, Timestamp: {}",
                userId, email, LocalDateTime.now());
    }
}