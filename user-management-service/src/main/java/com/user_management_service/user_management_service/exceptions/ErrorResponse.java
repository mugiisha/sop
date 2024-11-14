package com.user_management_service.user_management_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private List<ValidationError> validationErrors;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.validationErrors = null;
    }
}