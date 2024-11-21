package com.user_management_service.user_management_service.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
}