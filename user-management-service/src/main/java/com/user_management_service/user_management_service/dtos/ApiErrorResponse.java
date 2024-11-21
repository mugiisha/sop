package com.user_management_service.user_management_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class ApiErrorResponse {
    private ErrorResponse error;

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String message;
        private List<FieldError> errors;
    }

    @Data
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}