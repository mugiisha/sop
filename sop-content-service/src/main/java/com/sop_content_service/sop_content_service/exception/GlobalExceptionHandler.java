package com.sop_content_service.sop_content_service.exception;

import com.sop_content_service.sop_content_service.dto.ApiResponse;
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

    @ExceptionHandler(SopNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleSopNotFoundException(SopNotFoundException ex) {
        ApiResponse<String> errorResponse = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidInputException(InvalidInputException ex) {
        ApiResponse<String> errorResponse = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<String>> handleFileUploadException(FileUploadException ex) {
        ApiResponse<String> errorResponse = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("errorMessage", "An unexpected error occurred");
        return response;
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
