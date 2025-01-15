package com.user_management_service.user_management_service.exceptions;

public class DataIntegrityException extends DepartmentException {
    public DataIntegrityException(String message, String errorCode) {
        super(message, errorCode);
    }
}