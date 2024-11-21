package com.user_management_service.user_management_service.repositories;

import com.user_management_service.user_management_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<User, UUID> {
    // Authentication related queries
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByResetToken(String token);
    boolean existsByEmail(String email);

    // Security related queries
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.lastFailedLogin > :lockoutDate")
    List<User> findLockedUsers(@Param("maxAttempts") int maxAttempts, @Param("lockoutDate") LocalDateTime lockoutDate);

    // Email verification queries
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.emailVerificationTokenExpiry < :now")
    List<User> findByExpiredVerificationTokens(@Param("now") LocalDateTime now);

    // Password reset related queries
    @Query("SELECT u FROM User u WHERE u.resetTokenExpiry < :now AND u.resetToken IS NOT NULL")
    List<User> findExpiredResetTokens(@Param("now") LocalDateTime now);

    @Query("UPDATE User u SET u.resetToken = NULL, u.resetTokenExpiry = NULL WHERE u.resetTokenExpiry < :now")
    int cleanupExpiredResetTokens(@Param("now") LocalDateTime now);

    @Query("UPDATE User u SET u.emailVerificationToken = NULL, u.emailVerificationTokenExpiry = NULL " +
            "WHERE u.emailVerified = true AND u.emailVerificationToken IS NOT NULL")
    int cleanupVerifiedTokens();
}