package com.version_control_service.version_control_service.exception;

import com.version_control_service.version_control_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SopNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleSopNotFoundException(SopNotFoundException ex) {
        return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleGenericException(Exception ex) {
        return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", null);
    }
}
