package com.user_management_service.user_management_service.exceptions;

// Create new PasswordMismatchException.java
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
