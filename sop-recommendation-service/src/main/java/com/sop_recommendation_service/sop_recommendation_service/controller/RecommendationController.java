package com.sop_recommendation_service.sop_recommendation_service.controller;

import com.sop_recommendation_service.sop_recommendation_service.dtos.*;
import com.sop_recommendation_service.sop_recommendation_service.service.RecommendationService;
import com.sop_recommendation_service.sop_recommendation_service.service.GeminiSopGenerationService;
import com.sop_recommendation_service.sop_recommendation_service.service.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "SOP Services", description = "SOP Recommendation and Generation API")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final GeminiSopGenerationService aiService;
    private final RequestValidator requestValidator;

    @Operation(summary = "Get personalized SOP recommendations")
    @GetMapping("/recommendations/personalized")
    public Mono<ResponseEntity<ApiResponse<RecommendationResponse>>> getPersonalizedRecommendations(
            @RequestHeader("Authorization") String token) {
        return requestValidator.validateToken(token)
                .flatMap(recommendationService::getPersonalizedRecommendations)
                .map(recommendations -> ResponseEntity.ok(
                        new ApiResponse<>("Successfully generated personalized recommendations", recommendations)
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError().body(
                                new ApiResponse<>("Error generating recommendations: " + e.getMessage(), null)
                        )
                ));
    }

    @Operation(summary = "Generate response using AI")
    @PostMapping("/ai/generate")
    public Mono<ResponseEntity<ApiResponse<AiResponse>>> generateAiResponse(
            @RequestHeader("Authorization") String token,
            @RequestBody AiPromptRequest request) {
        return requestValidator.validateToken(token)
                .flatMap(tokenData -> aiService.generateResponse(request.getPrompt(), request.getType()))
                .map(result -> ResponseEntity.ok(
                        new ApiResponse<>(getSuccessMessage(request.getType()), result)
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError().body(
                                new ApiResponse<>("Error: " + e.getMessage(), null)
                        )
                ));
    }

    private String getSuccessMessage(String type) {
        return "SOP".equalsIgnoreCase(type)
                ? "Successfully generated SOP"
                : "Successfully generated response";
    }
}