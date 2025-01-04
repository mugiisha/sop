package com.sop_recommendation_service.sop_recommendation_service.controller;

import com.sop_recommendation_service.sop_recommendation_service.dtos.ApiResponse;
import com.sop_recommendation_service.sop_recommendation_service.service.RecommendationService;
import com.sop_recommendation_service.sop_recommendation_service.service.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "SOP Recommendation APIs")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final RequestValidator requestValidator;

    @Operation(summary = "Get personalized SOP recommendations")
    @GetMapping("/personalized")
    public Mono<ResponseEntity<ApiResponse<List<?>>>> getPersonalizedRecommendations(
            @RequestHeader("Authorization") String token) {
        return requestValidator.validateToken(token)
                .flatMap(recommendationService::getPersonalizedRecommendations)
                .map(recommendations -> ResponseEntity.ok(
                        new ApiResponse<>("Successfully generated recommendations", recommendations)
                ));
    }

    @Operation(summary = "Get similar SOPs based on a reference SOP")
    @GetMapping("/similar/{sopId}")
    public Mono<ResponseEntity<ApiResponse<List<?>>>> getSimilarSOPs(
            @PathVariable String sopId,
            @RequestHeader("Authorization") String token) {
        return Mono.zip(
                        requestValidator.validateToken(token),
                        requestValidator.validateSopId(sopId)
                )
                .flatMap(tuple -> recommendationService.getSimilarSOPs(tuple.getT2(), tuple.getT1()))
                .map(recommendations -> ResponseEntity.ok(
                        new ApiResponse<>("Successfully found similar SOPs", recommendations)
                ));
    }

    @Operation(summary = "Get recommendations by department")
    @GetMapping("/department/{departmentId}")
    public Mono<ResponseEntity<ApiResponse<List<?>>>> getDepartmentRecommendations(
            @PathVariable String departmentId,
            @RequestHeader("Authorization") String token) {
        return requestValidator.validateToken(token)
                .flatMap(validToken -> recommendationService.getDepartmentRecommendations(departmentId, validToken))
                .map(recommendations -> ResponseEntity.ok(
                        new ApiResponse<>("Successfully generated department recommendations", recommendations)
                ));
    }

    @Operation(summary = "Get trending SOPs")
    @GetMapping("/trending")
    public Mono<ResponseEntity<ApiResponse<List<?>>>> getTrendingSOPs(
            @RequestHeader("Authorization") String token) {
        return requestValidator.validateToken(token)
                .flatMap(recommendationService::getTrendingSOPs)
                .map(recommendations -> ResponseEntity.ok(
                        new ApiResponse<>("Successfully retrieved trending SOPs", recommendations)
                ));
    }
}