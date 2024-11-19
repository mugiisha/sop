package com.sop_content_service.sop_content_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle Image upload failure
    @ExceptionHandler(SopException.ImageUploadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleImageUploadException(SopException.ImageUploadException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("errorMessage", "Image upload failed ");
        return response;
    }

    // Handle Document upload failure
    @ExceptionHandler(SopException.DocumentUploadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleDocumentUploadException(SopException.DocumentUploadException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("errorMessage", "Document upload failed ");
        return response;
    }

    // Handle other exceptions (e.g., validation, database errors)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("errorMessage", "An unexpected error occurred ");
        return response;
    }
}
