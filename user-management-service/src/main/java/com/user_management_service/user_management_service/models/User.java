package com.user_management_service.user_management_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    @Column
    private LocalDateTime emailVerificationTokenExpiry;

    @Column
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    @Column
    private LocalDateTime lastLogin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deactivatedAt;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @Column
    private LocalDateTime lastFailedLogin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_user_department"))
    private Department department;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}