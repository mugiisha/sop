package com.sop_content_service.sop_content_service.exception;

public class InvalidSearchParameterException extends RuntimeException {
    public InvalidSearchParameterException(String message) {
        super(message);
    }
}