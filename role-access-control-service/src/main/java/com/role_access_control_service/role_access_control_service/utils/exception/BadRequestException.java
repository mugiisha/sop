package com.role_access_control_service.role_access_control_service.utils.exception;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message) {
        super(message);
    }
}
