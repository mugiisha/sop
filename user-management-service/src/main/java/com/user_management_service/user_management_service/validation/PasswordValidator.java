package com.user_management_service.user_management_service.validation;

import com.user_management_service.user_management_service.exceptions.InvalidPasswordException;

public class PasswordValidator {

    public static void validatePassword(String password) {
        if (password == null || password.length() < 12) {
            throw new InvalidPasswordException("Password must be at least 12 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new InvalidPasswordException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must contain at least one number");
        }
        if (!password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:,.<>/?].*")) {
            throw new InvalidPasswordException("Password must contain at least one special character");
        }
    }
}
