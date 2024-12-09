package com.notification_service.notification_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendWelcomeEmail(String toEmail, String name, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Our Platform!");
            message.setText("Dear " + name + ",\n\n" +
                    "Welcome to our platform! We're excited to have you on board.\n\n" +
                    "Please verify your email address to get started.\n\n" +
                    "Use this as your password: " + password + "\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }


    public void sendTemporaryPasswordEmail(String toEmail, String name, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Temporary Login Credentials");
            message.setText("Dear " + name + ",\n\n" +
                    "Your email has been verified successfully!\n\n" +
                    "Here are your temporary login credentials:\n" +
                    "Email: " + toEmail + "\n" +
                    "Temporary Password: " + temporaryPassword + "\n\n" +
                    "For security reasons, you will be required to change this password on your first login.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Temporary password email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send temporary password email to: {}", toEmail, e);
        }}


    public void sendVerificationEmail(String toEmail, String name, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Our Platform - Verify Your Email");
            message.setText("Dear " + name + ",\n\n" +
                    "Welcome to our platform! To complete your registration, please:\n\n" +
                    "Verify your email by clicking on the following link:\n" +
                    baseUrl + "/api/v1/users/verify-email/" + verificationToken + "\n\n" +
                    "This verification link and temporary password will expire in 24 hours.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }
    public void sendVerificationConfirmationEmail(String toEmail, String name, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification Successful");
            message.setText("Dear " + name + ",\n\n" +
                    "Your email has been successfully verified!\n\n" +
                    "Use these temporary credentials to log in:\n" +
                    "   Email: " + toEmail + "\n" +
                    "   Temporary Password: " + temporaryPassword + "\n\n" +
                    "For security reasons, you will be required to change your password upon first login.\n\n" +
                    "You can now log in using your email and temporary password.\n" +
                    "Remember to change your password upon first login.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Verification confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification confirmation email to: {}", toEmail, e);
        }
    }



    public void sendPasswordChangeConfirmation(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Changed Successfully");
            message.setText("Dear " + name + ",\n\n" +
                    "Your password has been successfully changed.\n\n" +
                    "If you did not make this change, please contact our support team immediately.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Password change confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation email to: {}", toEmail, e);
        }
    }











    public void sendPasswordResetRequestEmail(String toEmail, String name, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request");
            message.setText("Dear " + name + ",\n\n" +
                    "We received a request to reset your password. Here is your OTP:\n\n" +
                    otp + "\n\n" +
                    "This OTP will expire in 15 minutes. If you didn't request this change, " +
                    "please ignore this email or contact our support team.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Password reset request email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset request email to: {}", toEmail, e);
        }
    }

    public void sendPasswordResetConfirmation(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Confirmation");
            message.setText("Dear User,\n\n" +
                    "Your password has been successfully reset.\n\n" +
                    "If you did not request this change, please contact our support team immediately.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Password reset confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", toEmail, e);
        }
    }

    public void sendAccountLockedEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Account Locked - Multiple Failed Login Attempts");
            message.setText("Dear " + name + ",\n\n" +
                    "Your account has been locked due to multiple failed login attempts. " +
                    "This is a security measure to protect your account.\n\n" +
                    "To unlock your account, please use the password reset function or contact our support team.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Account locked email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send account locked email to: {}", toEmail, e);
        }
    }

    public void sendAccountDeactivationEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Account Deactivation Notice");
            message.setText("Dear " + name + ",\n\n" +
                    "Your account has been deactivated. If you believe this is a mistake, " +
                    "please contact our support team.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Account deactivation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send account deactivation email to: {}", toEmail, e);
        }
    }

    public void sendPasswordChangeNotification(String email) {
        sendPasswordChangeConfirmation(email, "User"); // Reuse existing method
    }




    public void sendOtpEmail(String toEmail, String name, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your One-Time Password (OTP)");
            message.setText("Dear " + name + ",\n\n" +
                    "Your OTP is: " + otp + "\n\n" +
                    "This OTP will expire in 15 minutes. Do not share this OTP with anyone.\n\n" +
                    "If you didn't request this OTP, please ignore this email or contact our support team.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
        }
    }
}