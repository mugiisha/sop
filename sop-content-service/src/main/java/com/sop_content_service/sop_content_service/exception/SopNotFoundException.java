package com.sop_content_service.sop_content_service.exception;

public class SopNotFoundException extends RuntimeException {
    public SopNotFoundException(String message) {
        super(message);
    }
}