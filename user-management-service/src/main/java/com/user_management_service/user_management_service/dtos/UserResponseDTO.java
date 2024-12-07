package com.user_management_service.user_management_service.dtos;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private UUID departmentId;
    private String departmentName;
    private String profilePictureUrl;
    private boolean active;
    private boolean emailVerified;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert the Role enum to a Spring Security GrantedAuthority
        if (roleName != null) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + roleName)
            );
        }
        // Return empty list if no role is assigned
        return Collections.emptyList();
    }
}