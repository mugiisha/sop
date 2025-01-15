package com.user_management_service.user_management_service.exceptions;

public class DepartmentValidationException extends DepartmentException {
    public DepartmentValidationException(String message, String errorCode) {
        super(message, errorCode);
    }
}