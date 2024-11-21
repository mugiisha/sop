package com.user_management_service.user_management_service.exceptions;

public class PasswordResetException extends RuntimeException {
    public PasswordResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
