package com.user_management_service.user_management_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentCreationDTO {
    @NotBlank(message = "Department name is required")
    private String name;

    private String description;
}