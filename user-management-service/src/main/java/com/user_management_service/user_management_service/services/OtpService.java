package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.CustomUserDto;
import com.user_management_service.user_management_service.models.Otp;
import com.user_management_service.user_management_service.repositories.OtpRepository;
import com.user_management_service.user_management_service.exceptions.OtpLimitExceededException;
import com.user_management_service.user_management_service.exceptions.InvalidOtpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    @Value("${otp.max-attempts}")
    private int maxOtpAttempts;

    @Value("${otp.attempt-window-hours}")
    private int otpAttemptWindowHours;

    public void generateAndSendOtp(String email, String name) {
        validateEmail(email);
        checkAttemptLimits(email);

        String otp = generateSecureOtp();
        saveOtp(email, otp);

        CustomUserDto customUserDto = new CustomUserDto();
        customUserDto.setEmail(email);
        customUserDto.setName(name);
        customUserDto.setPassword(otp);
        kafkaTemplate.send("otp-request", customUserDto);
    }

    public boolean verifyOtp(String email, String otp) {
        validateEmail(email);
        validateOtp(otp);

        Optional<Otp> otpEntity = otpRepository.findByEmailAndOtpAndExpiryTimeAfter(
                email,
                otp,
                LocalDateTime.now()
        );

        if (otpEntity.isEmpty()) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        otpRepository.delete(otpEntity.get());
        return true;
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
    }

    private void validateOtp(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be empty");
        }
    }

    private void checkAttemptLimits(String email) {
        long recentAttempts = otpRepository.countByEmailAndCreatedAtAfter(
                email,
                LocalDateTime.now().minusHours(otpAttemptWindowHours)
        );

        if (recentAttempts >= maxOtpAttempts) {
            throw new OtpLimitExceededException(
                    "Maximum OTP attempts exceeded. Please try again later."
            );
        }
    }

    private String generateSecureOtp() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    private void saveOtp(String email, String otp) {
        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        otpRepository.save(otpEntity);
    }
}