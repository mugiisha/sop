package com.sop_recommendation_service.sop_recommendation_service.service;

import com.sop_recommendation_service.sop_recommendation_service.dtos.SOPGenerationRequestDTO;
import com.sop_recommendation_service.sop_recommendation_service.dtos.SOPGenerationResponseDTO;
import com.sop_recommendation_service.sop_recommendation_service.models.SOP;
import com.sop_recommendation_service.sop_recommendation_service.repository.SOPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SOPGenerationService {

    private final SOPRepository sopRepository;
    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public Mono<SOPGenerationResponseDTO> generateSOP(SOPGenerationRequestDTO request) {
        return validateRequest(request)
                .flatMap(validRequest -> generateSOPContent(validRequest)
                        .flatMap(generatedContent -> {
                            SOP sop = createSOPFromRequest(request, generatedContent);
                            return sopRepository.save(sop)
                                    .map(this::createSuccessResponse)
                                    .onErrorResume(e -> Mono.error(new RuntimeException("Failed to save SOP: " + e.getMessage())));
                        }))
                .onErrorResume(e -> {
                    log.error("Error generating SOP: ", e);
                    SOPGenerationResponseDTO errorResponse = new SOPGenerationResponseDTO();
                    errorResponse.setStatus("ERROR");
                    errorResponse.setMessage("Failed to generate SOP: " + e.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    private Mono<SOPGenerationRequestDTO> validateRequest(SOPGenerationRequestDTO request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Title is required"));
        }
        if (request.getDepartment() == null || request.getDepartment().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Department is required"));
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Category is required"));
        }
        return Mono.just(request);
    }

    private Mono<String> generateSOPContent(SOPGenerationRequestDTO request) {
        String prompt = buildSOPPrompt(request);
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "safetySettings", List.of(
                        Map.of(
                                "category", "HARM_CATEGORY_HARASSMENT",
                                "threshold", "BLOCK_NONE"
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", 2048
                )
        );

        return webClient.post()
                .uri(geminiApiUrl)
                .header("X-Goog-Api-Key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> log.debug("Raw Gemini API response: {}", response))
                .map(this::extractGeneratedContent)
                .onErrorResume(e -> {
                    log.error("Error calling Gemini API: ", e);
                    return Mono.error(new RuntimeException("Failed to generate SOP content: " + e.getMessage()));
                });
    }

    private String buildSOPPrompt(SOPGenerationRequestDTO request) {
        return String.format("""
            Generate a detailed Standard Operating Procedure (SOP) with the following specifications:
            
            Title: %s
            Department: %s
            Category: %s
            Purpose: %s
            Scope: %s
            Keywords: %s
            
            Please format the SOP with the following sections and use clear, professional language:
            
            1. Purpose and Scope
            - Clearly state the purpose of this SOP
            - Define the scope and applicability
            
            2. Responsibilities
            - List key roles and their responsibilities
            - Include all relevant stakeholders
            
            3. Definitions
            - Define any technical terms or acronyms
            - Include relevant industry-specific terminology
            
            4. Procedures
            - Provide detailed step-by-step instructions
            - Include any precautions or warnings
            - List required equipment or materials
            - Detail quality control measures
            
            5. Safety Considerations
            - List all relevant safety precautions
            - Include PPE requirements if applicable
            - Detail emergency procedures if relevant
            
            6. Related Documents
            - Reference related SOPs or documents
            - List applicable regulations or standards
            
            7. Quality Records
            - Specify required documentation
            - Detail record-keeping procedures
            
            8. References
            - List any external references or standards
            - Include relevant regulatory requirements
            
            Format the content in a clear, professional manner with proper formatting and numbering.
            """,
                request.getTitle(),
                request.getDepartment(),
                request.getCategory(),
                request.getPurpose(),
                request.getScope(),
                String.join(", ", request.getKeywords())
        );
    }

    private String extractGeneratedContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No content generated by the API");
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("Generated content is empty");
            }

            String text = (String) parts.get(0).get("text");
            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("Generated text is empty");
            }

            return text;
        } catch (ClassCastException e) {
            log.error("Error parsing API response: {}", response);
            throw new RuntimeException("Failed to parse API response: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while parsing API response: {}", response, e);
            throw new RuntimeException("Failed to process API response: " + e.getMessage());
        }
    }

    private SOP createSOPFromRequest(SOPGenerationRequestDTO request, String generatedContent) {
        SOP sop = new SOP();
        sop.setTitle(request.getTitle());
        sop.setContent(generatedContent);
        sop.setDepartment(request.getDepartment());
        sop.setCategory(request.getCategory());
        sop.setCreatedAt(LocalDateTime.now());
        sop.setUpdatedAt(LocalDateTime.now());
        sop.setStatus("DRAFT");
        sop.setVersion(1);
        sop.setTags(request.getKeywords());
        sop.setViewCount(0);
        return sop;
    }

    private SOPGenerationResponseDTO createSuccessResponse(SOP savedSOP) {
        SOPGenerationResponseDTO response = new SOPGenerationResponseDTO();
        response.setId(savedSOP.getId());
        response.setTitle(savedSOP.getTitle());
        response.setContent(savedSOP.getContent());
        response.setStatus(savedSOP.getStatus());
        response.setMessage("SOP generated successfully");
        return response;
    }

    public Mono<SOP> getSOP(String id) {
        return sopRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("SOP not found with id: " + id)));
    }

    public Flux<SOP> getSOPsByDepartment(String department) {
        return sopRepository.findByDepartment(department);
    }
}