package com.sop_recommendation_service.sop_recommendation_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sop_recommendation_service.sop_recommendation_service.exceptions.RecommendationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import sopFromWorkflow.SopDetails;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiClientService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.max.prompt.length:800000000}")
    private int maxPromptLength;

    public GeminiClientService(@Value("${gemini.api.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> generateRecommendations(String promptText) {
        if (promptText == null || promptText.trim().isEmpty()) {
            return Mono.error(new RecommendationException("Empty prompt text", null));
        }

        if (promptText.length() > maxPromptLength) {
            return Mono.error(new RecommendationException(
                    String.format("Prompt text exceeds maximum length of %d characters", maxPromptLength),
                    null
            ));
        }

        ObjectNode requestBody = createRequestBody(promptText);
        return webClient.post()
                .uri(uri -> uri.queryParam("key", apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RecommendationException(
                                        String.format("Gemini API client error (4xx): %s - %s",
                                                response.statusCode(), error),
                                        null)))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RecommendationException(
                                        String.format("Gemini API server error (5xx): %s - %s",
                                                response.statusCode(), error),
                                        null)))
                )
                .bodyToMono(JsonNode.class)
                .map(this::extractRecommendationsFromResponse)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                        .filter(this::shouldRetry)
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying Gemini API request. Attempt {}/{}. Last error: {}",
                                        retrySignal.totalRetries() + 1,
                                        MAX_RETRIES,
                                        retrySignal.failure().getMessage())
                        ))
                .onErrorResume(this::handleError)
                .doOnNext(response -> log.debug("Raw Gemini response: {}", response));
    }

    public String buildPersonalizedPrompt(String userRole, String departmentName, List<SopDetails> sops) {
        if (userRole == null || userRole.trim().isEmpty()) {
            throw new RecommendationException("User role is required", null);
        }
        if (departmentName == null || departmentName.trim().isEmpty()) {
            throw new RecommendationException("Department name is required", null);
        }
        if (sops == null || sops.isEmpty()) {
            throw new RecommendationException("SOPs list cannot be empty", null);
        }

        try {
            return String.format("""
                You are a JSON-generating assistant. Your response must be ONLY valid JSON matching this exact structure:
                
                {
                    "recommendations": [
                        {
                            "title": String matching exactly one of the provided SOP titles,
                            "score": Number between 0 and 1,
                            "reason": String explaining the recommendation
                        }
                    ]
                }
                
                Context for generating recommendations:
                - User Role: %s
                - Department: %s
                
                Available SOPs:
                %s
                
                Instructions:
                1. Analyze each SOP's content, category, and description
                2. Consider the user's role and department context
                3. Score based on relevance, importance, and applicability
                4. Provide detailed reasoning for each recommendation
                5. Focus on SOPs that are most relevant to the user's role
                
                Rules:
                1. Response must be pure JSON - no other text
                2. All scores must be between 0 and 1
                3. Each recommendation must have exactly 3 fields as shown above
                4. Recommendations array must not be empty
                5. The title field must exactly match one of the provided SOP titles
                6. Do not include any explanatory text or markdown
                """,
                    sanitizeInput(userRole),
                    sanitizeInput(departmentName),
                    formatDetailedSopsList(sops));
        } catch (Exception e) {
            throw new RecommendationException("Failed to build personalized prompt", e);
        }
    }

    public String buildSimilarityPrompt(SopDetails sourceSop, List<SopDetails> allSops) {
        if (sourceSop == null) {
            throw new RecommendationException("Source SOP is required", null);
        }
        if (allSops == null || allSops.isEmpty()) {
            throw new RecommendationException("SOPs list cannot be empty", null);
        }

        try {
            List<SopDetails> filteredSops = allSops.stream()
                    .filter(sop -> !sop.getSopId().equals(sourceSop.getSopId()))
                    .collect(Collectors.toList());

            if (filteredSops.isEmpty()) {
                throw new RecommendationException("No other SOPs available for comparison", null);
            }

            return String.format("""
                You are a JSON-generating assistant. Your response must be ONLY valid JSON matching this exact structure:
                
                {
                    "similarSops": [
                        {
                            "title": String matching exactly one of the provided SOP titles,
                            "similarityScore": Number between 0 and 1,
                            "reason": String explaining the similarity
                        }
                    ]
                }
                
                Reference SOP:
                %s
                
                Available SOPs for Comparison:
                %s
                
                Instructions:
                1. Compare content, category, and purpose
                2. Consider workflow and process similarities
                3. Evaluate departmental relationships
                4. Look for shared standards or requirements
                
                Rules:
                1. Response must be pure JSON - no other text
                2. All similarityScores must be between 0 and 1
                3. Each similarSop must have exactly 3 fields as shown above
                4. SimilarSops array must not be empty
                5. The title field must exactly match one of the provided SOP titles
                6. Do not include any explanatory text or markdown
                """,
                    formatDetailedSopInfo(sourceSop),
                    formatDetailedSopsList(filteredSops));
        } catch (Exception e) {
            throw new RecommendationException("Failed to build similarity prompt", e);
        }
    }

    public String buildDepartmentPrompt(String departmentId, List<SopDetails> sops) {
        if (departmentId == null || departmentId.trim().isEmpty()) {
            throw new RecommendationException("Department ID is required", null);
        }
        if (sops == null || sops.isEmpty()) {
            throw new RecommendationException("SOPs list cannot be empty", null);
        }

        try {
            return String.format("""
                You are a JSON-generating assistant. Your response must be ONLY valid JSON matching this exact structure:
                
                {
                    "recommendations": [
                        {
                            "title": String matching exactly one of the provided SOP titles,
                            "score": Number between 0 and 1,
                            "reason": String explaining the recommendation
                        }
                    ]
                }
                
                Department Context:
                Department ID: %s
                
                Available Department SOPs:
                %s
                
                Evaluation Factors:
                - Department-specific workflows
                - Cross-departmental dependencies
                - Recent updates or changes
                - Compliance requirements
                - Process importance
                
                Rules:
                1. Response must be pure JSON - no other text
                2. All scores must be between 0 and 1
                3. Each recommendation must have exactly 3 fields as shown above
                4. Recommendations array must not be empty
                5. The title field must exactly match one of the provided SOP titles
                6. Do not include any explanatory text or markdown
                """,
                    sanitizeInput(departmentId),
                    formatDetailedSopsList(sops));
        } catch (Exception e) {
            throw new RecommendationException("Failed to build department prompt", e);
        }
    }

    public String buildTrendingPrompt(List<SopDetails> sops) {
        if (sops == null || sops.isEmpty()) {
            throw new RecommendationException("SOPs list cannot be empty", null);
        }

        try {
            return String.format("""
                You are a JSON-generating assistant. Your response must be ONLY valid JSON matching this exact structure:
                
                {
                    "recommendations": [
                        {
                            "title": String matching exactly one of the provided SOP titles,
                            "score": Number between 0 and 1,
                            "reason": String explaining the trending status
                        }
                    ]
                }
                
                Available SOPs:
                %s
                
                Trending Factors to Consider:
                - Recent updates or modifications
                - Critical business processes
                - Cross-departmental impact
                - Compliance requirements
                - User engagement patterns
                - Process importance
                
                Rules:
                1. Response must be pure JSON - no other text
                2. All scores must be between 0 and 1
                3. Each recommendation must have exactly 3 fields as shown above
                4. Recommendations array must not be empty
                5. The title field must exactly match one of the provided SOP titles
                6. Do not include any explanatory text or markdown
                """,
                    formatDetailedSopsList(sops));
        } catch (Exception e) {
            throw new RecommendationException("Failed to build trending prompt", e);
        }
    }

    private String formatDetailedSopsList(List<SopDetails> sops) {
        return sops.stream()
                .map(this::formatDetailedSopInfo)
                .collect(Collectors.joining("\n\n"));
    }

    private String formatDetailedSopInfo(SopDetails sop) {
        return String.format("""
            --- SOP Details ---
            Title: %s
            ID: %s
            Description: %s
            Body: %s
            Category: %s
            Department: %s
            Visibility: %s
            Status: %s
            Created: %s
            Updated: %s
            """,
                sanitizeInput(sop.getTitle()),
                sanitizeInput(sop.getSopId()),
                sanitizeInput(sop.getDescription()),
                sanitizeInput(sop.getBody()),
                sanitizeInput(sop.getCategory()),
                sanitizeInput(sop.getDepartmentId()),
                sanitizeInput(sop.getVisibility()),
                sanitizeInput(sop.getStatus()),
                sanitizeInput(sop.getCreatedAt()),
                sanitizeInput(sop.getUpdatedAt())
        );
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[\"\\\\]", "")
                .replaceAll("[\n\r]", " ")
                .trim();
    }

    private ObjectNode createRequestBody(String promptText) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode contents = objectMapper.createObjectNode();
            contents.put("role", "user");
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", promptText);
            contents.set("parts", objectMapper.createArrayNode().add(textPart));
            requestBody.set("contents", objectMapper.createArrayNode().add(contents));

            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 8192);
            generationConfig.put("topK", 1);
            generationConfig.put("topP", 0.8);
            generationConfig.put("candidateCount", 1);
            requestBody.set("generationConfig", generationConfig);

            ObjectNode safetySettings = objectMapper.createObjectNode();
            safetySettings.put("category", "HARM_CATEGORY_HARASSMENT");
            safetySettings.put("threshold", "BLOCK_NONE");
            requestBody.set("safetySettings", objectMapper.createArrayNode().add(safetySettings));

            return requestBody;
        } catch (Exception e) {
            throw new RecommendationException("Failed to create Gemini API request body", e);
        }
    }

    private String extractRecommendationsFromResponse(JsonNode response) {
        try {
            if (response == null) {
                throw new RecommendationException("Null response from Gemini API", null);
            }

            JsonNode candidates = response.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RecommendationException("No candidates found in Gemini API response", null);
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.path("content");
            if (!content.isObject()) {
                throw new RecommendationException("Invalid content structure in Gemini API response", null);
            }

            JsonNode parts = content.path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new RecommendationException("No parts found in Gemini API response", null);
            }

            String text = parts.get(0).path("text").asText();
            if (text == null || text.trim().isEmpty()) {
                throw new RecommendationException("Empty text in Gemini API response", null);
            }

            try {
                JsonNode jsonResponse = objectMapper.readTree(text);
                if (jsonResponse.has("recommendations") || jsonResponse.has("similarSops")) {
                    return text;
                }
            } catch (Exception e) {
                int jsonStart = text.indexOf("{");
                int jsonEnd = text.lastIndexOf("}");
                if (jsonStart >= 0 && jsonEnd >jsonStart) {
                    String jsonPart = text.substring(jsonStart, jsonEnd + 1);
                    JsonNode extracted = objectMapper.readTree(jsonPart);
                    if (extracted.has("recommendations") || extracted.has("similarSops")) {
                        return jsonPart;
                    }
                }
            }

            throw new RecommendationException("Response does not contain valid JSON structure", null);
        } catch (Exception e) {
            if (e instanceof RecommendationException) {
                throw (RecommendationException) e;
            }
            throw new RecommendationException("Error processing Gemini API response", e);
        }
    }

    private Mono<String> handleError(Throwable error) {
        if (error instanceof RecommendationException) {
            log.error("Recommendation generation failed: {}", error.getMessage());
            return Mono.error(error);
        }

        String errorMessage = "Unexpected error in Gemini API client";
        if (error instanceof IllegalArgumentException) {
            errorMessage = "Invalid argument provided to Gemini API";
        } else if (error instanceof IllegalStateException) {
            errorMessage = "Invalid state in Gemini API client";
        }

        log.error(errorMessage, error);
        return Mono.error(new RecommendationException(errorMessage, error));
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof RecommendationException) {
            String message = throwable.getMessage().toLowerCase();
            return !message.contains("client error") &&
                    !message.contains("invalid") &&
                    !message.contains("empty") &&
                    !message.contains("exceed") &&
                    !message.contains("required");
        }

        // Retry on other types of errors (network issues, server errors, etc.)
        return true;
    }
}