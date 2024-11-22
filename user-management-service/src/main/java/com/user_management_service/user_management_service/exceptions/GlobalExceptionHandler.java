package com.user_management_service.user_management_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            UserAlreadyExistsException.class,
            DepartmentAlreadyExistsException.class,
            AuthenticationException.class,
            InvalidTokenException.class,
            InvalidOtpException.class,
            OtpExpiredException.class,
            OtpLimitExceededException.class,
            RoleServerException.class
    })
    public ResponseEntity<ErrorResponse> handleCustomExceptions(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof UserAlreadyExistsException ||
                ex instanceof DepartmentAlreadyExistsException) {
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof OtpLimitExceededException) {
            status = HttpStatus.TOO_MANY_REQUESTS;
        } else if (ex instanceof InvalidTokenException ||
                ex instanceof InvalidOtpException ||
                ex instanceof OtpExpiredException) {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(PasswordMismatchException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "Validation Error",
                Collections.singletonList(new ValidationError("confirmPassword", ex.getMessage()))
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordResetException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetException(PasswordResetException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "Password Reset Error",
                Collections.singletonList(new ValidationError("general", ex.getMessage()))
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}