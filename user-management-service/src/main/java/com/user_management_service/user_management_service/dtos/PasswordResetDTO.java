package com.user_management_service.user_management_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Data
public class PasswordResetDTO {
    @NotBlank(message = "New password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must be at least 8 characters long and contain both letters and numbers")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    public PasswordResetDTO(String newPass123, String newPass1231) {
    }
}