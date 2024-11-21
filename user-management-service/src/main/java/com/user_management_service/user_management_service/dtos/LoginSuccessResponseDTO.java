package com.user_management_service.user_management_service.dtos;

import lombok.Data;

@Data
public class LoginSuccessResponseDTO {
    private SuccessData success;

    @Data
    public static class SuccessData {
        private int status = 200;
        private String message = "Login successful";
        private LoginData data;
    }

    @Data
    public static class LoginData {
        private String role;
        private String token;
        private UserDetailsDTO user;
    }
}