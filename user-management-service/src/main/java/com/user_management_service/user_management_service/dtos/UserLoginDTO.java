package com.user_management_service.user_management_service.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UserLoginDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public UserLoginDTO(String mail, String wrongpass) {
    }
}
