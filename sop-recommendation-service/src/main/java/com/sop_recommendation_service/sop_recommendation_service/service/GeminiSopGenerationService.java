package com.sop_recommendation_service.sop_recommendation_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_recommendation_service.sop_recommendation_service.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiSopGenerationService {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    public Mono<AiResponse> generateResponse(String prompt, String type) {
        try {
            return Mono.fromCallable(() -> {
                String fullPrompt = buildPrompt(prompt, type);
                String response = getGeminiResponse(fullPrompt);
                return AiResponse.builder()
                        .content(response)
                        .timestamp(System.currentTimeMillis())
                        .build();
            });
        } catch (Exception e) {
            log.error("Error generating response with Gemini", e);
            return Mono.error(new RuntimeException("Failed to generate response", e));
        }
    }

    private String buildPrompt(String userPrompt, String type) {
        if ("SOP".equalsIgnoreCase(type)) {
            return String.format("""
                You are an expert at creating Standard Operating Procedures (SOPs).
                Create a detailed SOP based on the following request:
                
                %s
                
                Generate a comprehensive SOP that includes:
                1. Clear title and purpose
                2. Scope and applicability
                3. Detailed procedure steps
                4. Safety considerations (if applicable)
                5. Quality control measures
                6. Required resources
                7. References (if needed)
                
                Format the response in markdown for better readability.
                """, userPrompt);
        } else {
            return String.format("""
                You are a helpful AI assistant. Please respond to the following request:
                
                %s
                
                Provide a clear and detailed response that addresses all aspects of the request.
                Format your response in a clear, readable way using markdown for better organization.
                """, userPrompt);
        }
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
}