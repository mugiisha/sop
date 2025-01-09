package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RecommendationResult {
    private final String title;
    private final double score;
    private final String reason;
}