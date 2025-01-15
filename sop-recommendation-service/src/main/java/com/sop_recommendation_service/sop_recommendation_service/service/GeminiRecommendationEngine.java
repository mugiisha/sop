package com.sop_recommendation_service.sop_recommendation_service.service;
import com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sopFromWorkflow.SopDetails;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiRecommendationEngine implements RecommendationEngine {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final double MINIMUM_SCORE_THRESHOLD = 0.5;

    @Override
    public List<RecommendationResult> generatePersonalizedRecommendations(
            String userRole, String departmentId, List<SopDetails> sops) {
        try {
            String prompt = buildPersonalizedPrompt(userRole, departmentId, sops);
            String response = getGeminiResponse(prompt);
            return parseRecommendationResponse(response);
        } catch (Exception e) {
            log.error("Error generating personalized recommendations with Gemini", e);
            throw new RuntimeException("Failed to generate recommendations", e);
        }
    }

    private String buildPersonalizedPrompt(String userRole, String departmentId, List<SopDetails> sops) {
        return String.format("""
            You are an AI recommendation system for Standard Operating Procedures (SOPs).
            Based on the following user context and available SOPs, recommend the 5 most relevant SOPs.
            
            User Context:
            - Role: %s
            - Department ID: %s
            
            Consider these factors for recommendations:
            1. Role relevance: How relevant is the SOP to the user's role
            2. Department alignment: Whether the SOP is from the user's department
            3. Content accessibility: Whether the content matches the user's expertise level
            4. Recent updates: Prioritize recently updated SOPs
            5. Status: Prioritize active SOPs
            
            Available SOPs:
            %s
            
            Return ONLY a JSON response with exactly 5 recommendations in this format (no additional text):
            {
                "recommendations": [
                    {
                        "title": "SOP Title",
                        "score": 0.95,
                        "reason": "Clear explanation of why this SOP is recommended"
                    }
                ]
            }
            """,
                userRole,
                departmentId,
                formatSopsForPrompt(sops)
        );
    }

    private String formatSopsForPrompt(List<SopDetails> sops) {
        return sops.stream()
                .map(sop -> String.format("""
                Title: %s
                Category: %s
                Department: %s
                Description: %s
                Status: %s
                UpdatedAt: %s
                """,
                        sop.getTitle(),
                        sop.getCategory(),
                        sop.getDepartmentId(),
                        sop.getDescription(),
                        sop.getStatus(),
                        sop.getUpdatedAt()
                ))
                .collect(Collectors.joining("\n---\n"));
    }

    private String getGeminiResponse(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> promptPart = new HashMap<>();

            promptPart.put("text", prompt);
            content.put("parts", Collections.singletonList(promptPart));
            requestBody.put("contents", Collections.singletonList(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = GEMINI_API_URL + "?key=" + apiKey;

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> candidateContent = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> responseParts = (List<Map<String, Object>>) candidateContent.get("parts");
                    return (String) responseParts.get(0).get("text");
                }
            }

            throw new RuntimeException("Invalid response format from Gemini API");
        } catch (Exception e) {
            log.error("Error getting response from Gemini", e);
            throw new RuntimeException("Failed to get Gemini response", e);
        }
    }

    private List<RecommendationResult> parseRecommendationResponse(String response) {
        try {
            Map<String, List<Map<String, Object>>> jsonResponse =
                    objectMapper.readValue(response, Map.class);

            List<Map<String, Object>> recommendations =
                    jsonResponse.get("recommendations");

            return recommendations.stream()
                    .map(rec -> new RecommendationResult(
                            (String) rec.get("title"),
                            ((Number) rec.get("score")).doubleValue(),
                            (String) rec.get("reason")
                    ))
                    .filter(rec -> rec.getScore() >= MINIMUM_SCORE_THRESHOLD)
                    .sorted(Comparator.comparing(RecommendationResult::getScore).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            throw new RuntimeException("Failed to parse recommendations", e);
        }
    }
}