package com.user_management_service.user_management_service.exceptions;


public class DepartmentNotEmptyException extends RuntimeException {
    private final String errorCode;

    public DepartmentNotEmptyException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
