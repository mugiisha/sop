package com.role_access_control_service.role_access_control_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssignRoleDto {
    @NotBlank(message = "User id is required")
    private String userId;
    @NotBlank(message = "Role id is required")
    private String roleId;
    @NotBlank(message = "Department id is required")
    private String departmentId;
    private LocalDateTime assignedAt;
}
