package com.user_management_service.user_management_service.services;

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

    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Our Platform!");
            message.setText("Dear " + name + ",\n\n" +
                    "Welcome to our platform! We're excited to have you on board.\n\n" +
                    "Please verify your email address to get started.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    public void sendVerificationEmail(String toEmail, String name, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email");
            message.setText("Dear " + name + ",\n\n" +
                    "Please verify your email by clicking on the following link:\n" +
                    "http://yourplatform.com/verify-email/" + verificationToken + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    public void sendVerificationConfirmationEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification Successful");
            message.setText("Dear " + name + ",\n\n" +
                    "Your email has been successfully verified! You can now access all features of our platform.\n\n" +
                    "Best regards,\nThe Team");

            emailSender.send(message);
            log.info("Verification confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification confirmation email to: {}", toEmail, e);
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