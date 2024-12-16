package com.sop_workflow_service.sop_workflow_service.utils.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
