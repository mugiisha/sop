package com.sop_recommendation_service.sop_recommendation_service.exceptions;

public class RecommendationException extends RuntimeException {
    // For message only
    public RecommendationException(String message) {
        super(message);
    }

    // If you need to include a cause
    public RecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}