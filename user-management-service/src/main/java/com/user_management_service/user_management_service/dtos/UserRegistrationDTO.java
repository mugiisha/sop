package com.user_management_service.user_management_service.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
public class UserRegistrationDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Role ID is required")
    private UUID roleId;
}
