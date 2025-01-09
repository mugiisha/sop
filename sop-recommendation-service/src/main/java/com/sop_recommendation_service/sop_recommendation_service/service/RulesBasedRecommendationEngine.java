package com.sop_recommendation_service.sop_recommendation_service.service;

import com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sopFromWorkflow.SopDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RulesBasedRecommendationEngine implements RecommendationEngine {
    private static final double DEPARTMENT_MATCH_SCORE = 0.8;
    private static final double CATEGORY_MATCH_SCORE = 0.6;
    private static final double RECENT_UPDATE_SCORE = 0.7;
    private static final double ROLE_RELEVANCE_SCORE = 0.5;
    private static final long RECENT_THRESHOLD_DAYS = 30;

    @Override
    public List<RecommendationResult> generatePersonalizedRecommendations(
            String userRole, String departmentId, List<SopDetails> sops) {
        return sops.stream()
                .map(sop -> {
                    double score = calculatePersonalizedScore(sop, userRole, departmentId);
                    String reason = generatePersonalizedReason(sop, userRole, departmentId);
                    return new RecommendationResult(sop.getTitle(), score, reason);
                })
                .sorted(Comparator.comparing(RecommendationResult::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationResult> findSimilarSops(SopDetails sourceSop, List<SopDetails> allSops) {
        return allSops.stream()
                .filter(sop -> !sop.getSopId().equals(sourceSop.getSopId()))
                .map(sop -> {
                    double score = calculateSimilarityScore(sourceSop, sop);
                    String reason = generateSimilarityReason(sourceSop, sop);
                    return new RecommendationResult(sop.getTitle(), score, reason);
                })
                .sorted(Comparator.comparing(RecommendationResult::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationResult> generateDepartmentRecommendations(String departmentId, List<SopDetails> sops) {
        return sops.stream()
                .filter(sop -> sop.getDepartmentId().equals(departmentId))
                .map(sop -> {
                    double score = calculateDepartmentScore(sop);
                    String reason = generateDepartmentReason(sop);
                    return new RecommendationResult(sop.getTitle(), score, reason);
                })
                .sorted(Comparator.comparing(RecommendationResult::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationResult> getTrendingSops(List<SopDetails> sops) {
        return sops.stream()
                .map(sop -> {
                    double score = calculateTrendingScore(sop);
                    String reason = generateTrendingReason(sop);
                    return new RecommendationResult(sop.getTitle(), score, reason);
                })
                .sorted(Comparator.comparing(RecommendationResult::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private double calculatePersonalizedScore(SopDetails sop, String userRole, String departmentId) {
        double score = 0.0;

        // Department match
        if (sop.getDepartmentId().equals(departmentId)) {
            score += DEPARTMENT_MATCH_SCORE;
        }

        // Recent updates
        if (isRecentlyUpdated(sop)) {
            score += RECENT_UPDATE_SCORE;
        }

        // Role-based scoring
        if (isRelevantToRole(sop, userRole)) {
            score += ROLE_RELEVANCE_SCORE;
        }

        return Math.min(score, 1.0);
    }

    private double calculateSimilarityScore(SopDetails source, SopDetails target) {
        double score = 0.0;

        // Same department
        if (source.getDepartmentId().equals(target.getDepartmentId())) {
            score += 0.3;
        }

        // Same category
        if (source.getCategory().equals(target.getCategory())) {
            score += CATEGORY_MATCH_SCORE;
        }

        // Content similarity
        double contentSimilarity = calculateContentSimilarity(source, target);
        score += (0.4 * contentSimilarity);

        return Math.min(score, 1.0);
    }

    private double calculateDepartmentScore(SopDetails sop) {
        double score = 0.5; // Base score

        if (isRecentlyUpdated(sop)) {
            score += RECENT_UPDATE_SCORE;
        }

        // Add score based on SOP status
        if ("ACTIVE".equals(sop.getStatus())) {
            score += 0.3;
        }

        return Math.min(score, 1.0);
    }

    private double calculateTrendingScore(SopDetails sop) {
        double score = 0.0;

        // Recent updates get highest priority
        if (isRecentlyUpdated(sop)) {
            score += RECENT_UPDATE_SCORE;
        }

        // Active status
        if ("ACTIVE".equals(sop.getStatus())) {
            score += 0.3;
        }

        // Public visibility
        if ("PUBLIC".equals(sop.getVisibility())) {
            score += 0.2;
        }

        return Math.min(score, 1.0);
    }

    private double calculateContentSimilarity(SopDetails source, SopDetails target) {
        // Calculate similarity based on title and description
        double titleSimilarity = calculateWordSimilarity(
                tokenizeText(source.getTitle()),
                tokenizeText(target.getTitle())
        );

        double descSimilarity = calculateWordSimilarity(
                tokenizeText(source.getDescription()),
                tokenizeText(target.getDescription())
        );

        return (titleSimilarity * 0.6) + (descSimilarity * 0.4);
    }

    private Set<String> tokenizeText(String text) {
        if (text == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());
    }

    private double calculateWordSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private boolean isRecentlyUpdated(SopDetails sop) {
        try {
            LocalDateTime updateTime = LocalDateTime.parse(sop.getUpdatedAt());
            return updateTime.isAfter(LocalDateTime.now().minusDays(RECENT_THRESHOLD_DAYS));
        } catch (Exception e) {
            log.warn("Error parsing update time for SOP: {}", sop.getSopId());
            return false;
        }
    }

    private boolean isRelevantToRole(SopDetails sop, String userRole) {
        // Basic role relevance logic - can be enhanced based on specific requirements
        if (userRole.toLowerCase().contains("admin")) {
            return true;
        }

        if (userRole.toLowerCase().contains("manager")) {
            return !sop.getCategory().toLowerCase().contains("technical");
        }

        return sop.getCategory().toLowerCase().contains(userRole.toLowerCase()) ||
                sop.getTitle().toLowerCase().contains(userRole.toLowerCase());
    }

    private String generatePersonalizedReason(SopDetails sop, String userRole, String departmentId) {
        List<String> reasons = new ArrayList<>();

        if (sop.getDepartmentId().equals(departmentId)) {
            reasons.add("Relevant to your department");
        }
        if (isRecentlyUpdated(sop)) {
            reasons.add("Recently updated");
        }
        if (isRelevantToRole(sop, userRole)) {
            reasons.add("Matches your role requirements");
        }

        return reasons.isEmpty() ? "General recommendation" : String.join(", ", reasons);
    }

    private String generateSimilarityReason(SopDetails source, SopDetails target) {
        List<String> reasons = new ArrayList<>();

        if (source.getDepartmentId().equals(target.getDepartmentId())) {
            reasons.add("Same department");
        }
        if (source.getCategory().equals(target.getCategory())) {
            reasons.add("Similar category");
        }
        if (calculateContentSimilarity(source, target) > 0.5) {
            reasons.add("Related content");
        }

        return reasons.isEmpty() ? "General similarity" : String.join(", ", reasons);
    }

    private String generateDepartmentReason(SopDetails sop) {
        List<String> reasons = new ArrayList<>();

        if (isRecentlyUpdated(sop)) {
            reasons.add("Recently updated department SOP");
        }
        if ("ACTIVE".equals(sop.getStatus())) {
            reasons.add("Currently active");
        }

        return reasons.isEmpty() ? "Department-specific SOP" : String.join(", ", reasons);
    }

    private String generateTrendingReason(SopDetails sop) {
        List<String> reasons = new ArrayList<>();

        if (isRecentlyUpdated(sop)) {
            reasons.add("Recently updated");
        }
        if ("ACTIVE".equals(sop.getStatus())) {
            reasons.add("Active status");
        }
        if ("PUBLIC".equals(sop.getVisibility())) {
            reasons.add("Widely accessible");
        }

        return reasons.isEmpty() ? "Trending based on activity" : String.join(", ", reasons);
    }
}