package com.role_access_control_service.role_access_control_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class  AssignRoleRequest {
    @NotBlank(message = "Department id is required")
    private String departmentId;
}