package com.user_management_service.user_management_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String departmentName;
    private boolean active;
    private boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}