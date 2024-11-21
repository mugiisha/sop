package com.user_management_service.user_management_service.repositories;

import com.user_management_service.user_management_service.models.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime dateTime);

    Optional<Otp> findByEmailAndOtpAndExpiryTimeAfter(String email, String otp, LocalDateTime dateTime);
}
