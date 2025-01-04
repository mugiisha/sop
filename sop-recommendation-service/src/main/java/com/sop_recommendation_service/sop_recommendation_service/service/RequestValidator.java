package com.sop_recommendation_service.sop_recommendation_service.service;

import com.sop_recommendation_service.sop_recommendation_service.exceptions.RecommendationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestValidator {

    public Mono<String> validateToken(String token) {
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            return Mono.error(new RecommendationException("Invalid authorization token", null));
        }
        return Mono.just(token);
    }

    public Mono<String> validateSopId(String sopId) {
        if (sopId == null || sopId.trim().isEmpty()) {
            return Mono.error(new RecommendationException("SOP ID cannot be empty", null));
        }
        return Mono.just(sopId);
    }
}