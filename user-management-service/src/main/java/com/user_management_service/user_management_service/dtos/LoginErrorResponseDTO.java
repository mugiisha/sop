package com.user_management_service.user_management_service.dtos;

import lombok.Data;
import java.util.List;

@Data
public class LoginErrorResponseDTO {
    private ErrorData error;

    @Data
    public static class ErrorData {
        private int status;
        private String message;
        private List<ValidationError> errors;
    }

    @Data
    public static class ValidationError {
        private String field;
        private String message;

        public ValidationError(String confirmPassword, String passwordsDoNotMatch) {
        }
    }
}