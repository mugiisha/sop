package com.user_management_service.user_management_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class OtpLimitExceededException extends RuntimeException {
    public OtpLimitExceededException(String message) {
        super(message);
    }
}
