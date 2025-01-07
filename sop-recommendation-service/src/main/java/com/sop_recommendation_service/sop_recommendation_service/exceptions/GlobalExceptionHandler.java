package com.sop_recommendation_service.sop_recommendation_service.exceptions;

import com.sop_recommendation_service.sop_recommendation_service.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecommendationException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleRecommendationException(RecommendationException ex) {
        ApiResponse<String> response = new ApiResponse<>(
                ex.getMessage(),
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleGeneralException(Exception ex) {
        ApiResponse<String> response = new ApiResponse<>(
                "An unexpected error occurred",
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}