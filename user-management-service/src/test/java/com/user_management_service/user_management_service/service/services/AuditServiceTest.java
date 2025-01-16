package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.enums.Role;
import com.user_management_service.user_management_service.services.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private Logger log;

    @InjectMocks
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Captor
    private ArgumentCaptor<Object[]> argumentsCaptor;

    private UUID userId;
    private String email;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
    }

    @Test
    void logUserCreation_ShouldLogCorrectMessage() {
        auditService.logUserCreation(userId, email);

        verify(log).info(
                eq("User created - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logUserLogin_ShouldLogCorrectMessage() {
        auditService.logUserLogin(userId, email);

        verify(log).info(
                eq("User login - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logPasswordResetRequest_ShouldLogCorrectMessage() {
        auditService.logPasswordResetRequest(userId, email);

        verify(log).info(
                eq("Password reset requested - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logPasswordReset_ShouldLogCorrectMessage() {
        auditService.logPasswordReset(userId, email);

        verify(log).info(
                eq("Password reset completed - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logFailedLoginAttempt_ShouldLogCorrectMessage() {
        int attemptCount = 3;
        auditService.logFailedLoginAttempt(userId, email, attemptCount);

        verify(log).warn(
                eq("Failed login attempt - UserID: {}, Email: {}, Attempt #: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                eq(attemptCount),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logRoleChange_ShouldLogCorrectMessage() {
        Role oldRole = Role.USER;
        Role newRole = Role.ADMIN;
        auditService.logRoleChange(userId, email, oldRole, newRole);

        verify(log).info(
                eq("Role changed - UserID: {}, Email: {}, Old Role: {}, New Role: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                eq(oldRole),
                eq(newRole),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logAccountLocked_ShouldLogCorrectMessage() {
        auditService.logAccountLocked(userId, email);

        verify(log).info(
                eq("Account locked due to failed attempts - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logProfilePictureUpdate_ShouldLogCorrectMessage() {
        auditService.logProfilePictureUpdate(userId, email);

        verify(log).info(
                eq("Profile picture updated - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logUserUpdate_ShouldLogCorrectMessage() {
        auditService.logUserUpdate(userId, email);

        verify(log).info(
                eq("User updated - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logUserDeactivation_ShouldLogCorrectMessage() {
        auditService.logUserDeactivation(userId, email);

        verify(log).info(
                eq("User deactivated - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logNotificationPreferenceUpdate_ShouldLogCorrectMessage() {
        auditService.logNotificationPreferenceUpdate(userId, email);

        verify(log).info(
                eq("Notification preferences updated - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logEmailVerification_ShouldLogCorrectMessage() {
        auditService.logEmailVerification(userId, email);

        verify(log).info(
                eq("Email verified - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logPasswordChange_ShouldLogCorrectMessage() {
        auditService.logPasswordChange(userId, email);

        verify(log).info(
                eq("Password changed - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logVerificationTokenGenerated_ShouldLogCorrectMessage() {
        auditService.logVerificationTokenGenerated(userId, email);

        verify(log).info(
                eq("Verification token generated - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logVerificationEmailResent_ShouldLogCorrectMessage() {
        auditService.logVerificationEmailResent(userId, email);

        verify(log).info(
                eq("Verification email resent - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }

    @Test
    void logForcedEmailVerification_ShouldLogCorrectMessage() {
        auditService.logForcedEmailVerification(userId, email);

        verify(log).info(
                eq("Forced email verification - UserID: {}, Email: {}, Timestamp: {}"),
                eq(userId),
                eq(email),
                any(LocalDateTime.class)
        );
    }
}