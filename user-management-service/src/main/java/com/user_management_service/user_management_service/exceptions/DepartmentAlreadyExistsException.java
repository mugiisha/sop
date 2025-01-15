package com.user_management_service.user_management_service.exceptions;

public class DepartmentAlreadyExistsException extends DepartmentException {
    public DepartmentAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }
}