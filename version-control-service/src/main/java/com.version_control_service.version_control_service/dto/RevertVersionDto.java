package com.version_control_service.version_control_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RevertVersionDto {
    @NotNull(message = "Version Number is required")
    @DecimalMin(value = "1.0", message = "Version Number must be greater than or equal to 1.0")
    private Float versionNumber;
}
