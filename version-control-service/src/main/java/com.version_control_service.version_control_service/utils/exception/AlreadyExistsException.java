package com.version_control_service.version_control_service.utils.exception;



public class AlreadyExistsException  extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
