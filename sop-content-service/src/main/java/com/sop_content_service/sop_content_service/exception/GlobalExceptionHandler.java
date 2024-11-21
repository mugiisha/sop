package com.sop_content_service.sop_content_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Extract error messages directly
        List<String> errorMessages = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(objectError -> objectError.getDefaultMessage())
                .collect(Collectors.toList());

        // Return the first error message as "errorMessage"
        Map<String, String> response = new HashMap<>();
        response.put("errorMessage", errorMessages.get(0));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    // Handle SOP not found exception
    @ExceptionHandler(SopException.SopNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleSopNotFoundException(SopException.SopNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("errorMessage", ex.getMessage()); // Use the message from the exception
        return response;
    }

}
