package com.user_management_service.user_management_service.exceptions;

public class DepartmentStatusException extends DepartmentException {
    public DepartmentStatusException(String message, String errorCode) {
        super(message, errorCode);
    }
}