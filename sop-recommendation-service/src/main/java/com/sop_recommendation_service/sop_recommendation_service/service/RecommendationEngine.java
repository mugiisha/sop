package com.sop_recommendation_service.sop_recommendation_service.service;

import com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationResult;
import sopFromWorkflow.SopDetails;
import java.util.List;

public interface RecommendationEngine {
    List<RecommendationResult> generatePersonalizedRecommendations(
            String userRole,
            String departmentId,
            List<SopDetails> sops
    );

    List<RecommendationResult> findSimilarSops(
            SopDetails sourceSop,
            List<SopDetails> allSops
    );

    List<RecommendationResult> generateDepartmentRecommendations(
            String departmentId,
            List<SopDetails> sops
    );

    List<RecommendationResult> getTrendingSops(List<SopDetails> sops);
}