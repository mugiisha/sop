package com.notification_service.notification_service.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;

    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String name, String verificationToken) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name,
                    "frontendUrl", frontendUrl,
                    "token", verificationToken
            );
            sendEmailWithTemplate(toEmail, "Email Verification", "email-verification", templateModel);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    public void sendVerificationConfirmationEmail(String toEmail, String name, String temporaryPassword) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name,
                    "toEmail", toEmail,
                    "temporaryPassword", temporaryPassword
            );
            sendEmailWithTemplate(toEmail, "Email Verification Confirmation", "email-verification-confirmation", templateModel);
        } catch (Exception e) {
            log.error("Failed to send verification confirmation email to: {}", toEmail, e);
        }
    }

    public void sendPasswordChangeConfirmation(String toEmail, String name) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name
            );
            sendEmailWithTemplate(toEmail, "Password Change Confirmation", "password-change-confirmation", templateModel);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation email to: {}", toEmail, e);
        }
    }

    public void sendAccountLockedEmail(String toEmail, String name) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name
            );
            sendEmailWithTemplate(toEmail, "Account Locked", "account-locked", templateModel);
        } catch (Exception e) {
            log.error("Failed to send account locked email to: {}", toEmail, e);
        }
    }

    public void sendAccountDeactivationEmail(String toEmail, String name) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name
            );
          sendEmailWithTemplate(toEmail, "Account Deactivation", "account-deactivation", templateModel);
        } catch (Exception e) {
            log.error("Failed to send account deactivation email to: {}", toEmail, e);
        }
    }

    public void sendAccountActivationEmail(String toEmail, String name) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name
            );
            sendEmailWithTemplate(toEmail, "Account Activation", "account-activation", templateModel);
        } catch (Exception e) {
            log.error("Failed to send account deactivation email to: {}", toEmail, e);
        }
    }

    public void sendOtpEmail(String toEmail, String name, String otp) {
        try {
            Map<String, Object> templateModel = Map.of(
                    "name", name,
                    "otp", otp
            );

            sendEmailWithTemplate(toEmail, "Your One-Time Password (OTP)", "otp", templateModel);

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
        }
    }

    public void sendSOPWorkflowEmail(
            String toEmail,
            String name,
            String role,
            String subject,
            String sopTitle,
            String sopId,
            String templateName){
        try{
            Map<String, Object> templateModel = Map.of(
                    "name", name,
                    "ROLEREQUIRED", role,
                    "title", sopTitle,
                    "sopId", sopId,
                    "frontendUrl", frontendUrl
            );
            sendEmailWithTemplate(toEmail, subject, templateName, templateModel);
        } catch (Exception e) {
            log.error("Failed to send SOP workflow email to: {}", toEmail, e);
        }
    }


    public void sendEmailWithTemplate(String toEmail, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            Context context = new Context();
            context.setVariables(templateModel);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", toEmail, e);
        }
    }
}