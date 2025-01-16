package com.notification_service.notification_service.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String FROM_EMAIL = "from@example.com";
    private static final String FRONTEND_URL = "http://frontend.example.com";
    private static final String PROCESSED_TEMPLATE = "<html>Processed template</html>";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(PROCESSED_TEMPLATE);
    }

    @Test
    void sendVerificationEmail_Success() throws MessagingException {
        // Arrange
        String verificationToken = "test-token";

        // Act
        emailService.sendVerificationEmail(TEST_EMAIL, TEST_NAME, verificationToken);

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationConfirmationEmail_Success() throws MessagingException {
        // Arrange
        String temporaryPassword = "temp-pass";

        // Act
        emailService.sendVerificationConfirmationEmail(TEST_EMAIL, TEST_NAME, temporaryPassword);

        // Assert
        verify(templateEngine).process(eq("email-verification-confirmation"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordChangeConfirmation_Success() throws MessagingException {
        // Act
        emailService.sendPasswordChangeConfirmation(TEST_EMAIL, TEST_NAME);

        // Assert
        verify(templateEngine).process(eq("password-change-confirmation"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendAccountLockedEmail_Success() throws MessagingException {
        // Act
        emailService.sendAccountLockedEmail(TEST_EMAIL, TEST_NAME);

        // Assert
        verify(templateEngine).process(eq("account-locked"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendAccountDeactivationEmail_Success() throws MessagingException {
        // Act
        emailService.sendAccountDeactivationEmail(TEST_EMAIL, TEST_NAME);

        // Assert
        verify(templateEngine).process(eq("account-deactivation"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendAccountActivationEmail_Success() throws MessagingException {
        // Act
        emailService.sendAccountActivationEmail(TEST_EMAIL, TEST_NAME);

        // Assert
        verify(templateEngine).process(eq("account-activation"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOtpEmail_Success() throws MessagingException {
        // Arrange
        String otp = "123456";

        // Act
        emailService.sendOtpEmail(TEST_EMAIL, TEST_NAME, otp);

        // Assert
        verify(templateEngine).process(eq("otp"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendSOPWorkflowEmail_Success() throws MessagingException {
        // Arrange
        String role = "ADMIN";
        String subject = "SOP Workflow";
        String sopTitle = "Test SOP";
        String sopId = "SOP123";
        String templateName = "sop-workflow";

        // Act
        emailService.sendSOPWorkflowEmail(TEST_EMAIL, TEST_NAME, role, subject, sopTitle, sopId, templateName);

        // Assert
        verify(templateEngine).process(eq(templateName), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailWithTemplate_HandlesException() throws MessagingException {
        // Arrange
        doThrow(new MessagingException("Test exception"))
                .when(emailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendEmailWithTemplate(TEST_EMAIL, "Test Subject", "test-template", Map.of("key", "value"));

        // Assert
        verify(templateEngine).process(eq("test-template"), any(Context.class));
        verify(emailSender).send(any(MimeMessage.class));
    }
}