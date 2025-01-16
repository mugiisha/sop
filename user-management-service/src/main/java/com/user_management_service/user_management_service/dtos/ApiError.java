package com.user_management_service.user_management_service.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiError {
    private String errorCode;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    public ApiError(String errorCode, String message, int status, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}