// Base exception for all department-related exceptions
package com.user_management_service.user_management_service.exceptions;

public class DepartmentException extends RuntimeException {
    private final String errorCode;

    public DepartmentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
