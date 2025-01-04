package com.sop_recommendation_service.sop_recommendation_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationDTO;
import com.sop_recommendation_service.sop_recommendation_service.dtos.SimilarSopDTO;
import com.sop_recommendation_service.sop_recommendation_service.exceptions.RecommendationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import sopFromWorkflow.GetAllSopDetailsResponse;
import sopFromWorkflow.SopDetails;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final GeminiClientService geminiClientService;
    private final SopClientService sopClientService;
    private final UserInfoClientService userClientService;
    private final ObjectMapper objectMapper;

    public Mono<List<?>> getPersonalizedRecommendations(String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.zip(
                        Mono.just(tokenData),
                        Mono.fromCallable(() -> userClientService.getUserInfo(tokenData.get("sub").asText())),
                        Mono.fromCallable(() -> sopClientService.getAllSopDetails())
                ))
                .flatMap(tuple -> {
                    JsonNode tokenData = tuple.getT1();
                    GetAllSopDetailsResponse sopDetails = tuple.getT3();

                    if (sopDetails == null || sopDetails.getSopDetailsList() == null ||
                            sopDetails.getSopDetailsList().isEmpty()) {
                        log.warn("No SOPs found for recommendation generation");
                        return Mono.just(Collections.emptyList());
                    }

                    String prompt = geminiClientService.buildPersonalizedPrompt(
                            tokenData.get("role").asText(),
                            tokenData.get("departmentId").asText(),
                            sopDetails.getSopDetailsList()
                    );
                    return geminiClientService.generateRecommendations(prompt)
                            .map(this::parseRecommendations)
                            .doOnNext(recommendations ->
                                    log.debug("Generated {} personalized recommendations",
                                            recommendations.size()));
                })
                .onErrorResume(e -> {
                    log.error("Error generating personalized recommendations", e);
                    return Mono.error(new RecommendationException("Failed to generate personalized recommendations", e));
                });
    }

    public Mono<List<?>> getSimilarSOPs(String sopId, String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> sopClientService.getAllSopDetails())
                        .map(allSops -> Tuples.of(tokenData, allSops)))
                .flatMap(tuple -> {
                    GetAllSopDetailsResponse allSops = tuple.getT2();

                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return Mono.just(Collections.emptyList());
                    }

                    SopDetails sourceSop = findSourceSop(allSops, sopId);
                    String prompt = geminiClientService.buildSimilarityPrompt(
                            sourceSop,
                            allSops.getSopDetailsList()
                    );
                    return geminiClientService.generateRecommendations(prompt)
                            .map(this::parseSimilarSops)
                            .doOnNext(recommendations ->
                                    log.debug("Found {} similar SOPs",
                                            recommendations.size()));
                })
                .onErrorResume(e -> {
                    log.error("Error finding similar SOPs", e);
                    return Mono.error(new RecommendationException("Failed to find similar SOPs", e));
                });
    }

    public Mono<List<?>> getDepartmentRecommendations(String departmentId, String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> sopClientService.getAllSopDetails()))
                .flatMap(allSops -> {
                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return Mono.just(Collections.emptyList());
                    }

                    List<SopDetails> departmentSops = allSops.getSopDetailsList().stream()
                            .filter(sop -> sop.getDepartmentId().equals(departmentId))
                            .collect(Collectors.toList());

                    if (departmentSops.isEmpty()) {
                        return Mono.just(Collections.emptyList());
                    }

                    String prompt = geminiClientService.buildDepartmentPrompt(departmentId, departmentSops);
                    return geminiClientService.generateRecommendations(prompt)
                            .map(this::parseRecommendations)
                            .doOnNext(recommendations ->
                                    log.debug("Generated {} department recommendations",
                                            recommendations.size()));
                })
                .onErrorResume(e -> {
                    log.error("Error generating department recommendations", e);
                    return Mono.error(new RecommendationException("Failed to generate department recommendations", e));
                });
    }

    public Mono<List<?>> getTrendingSOPs(String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> sopClientService.getAllSopDetails()))
                .flatMap(allSops -> {
                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return Mono.just(Collections.emptyList());
                    }

                    String prompt = geminiClientService.buildTrendingPrompt(allSops.getSopDetailsList());
                    return geminiClientService.generateRecommendations(prompt)
                            .map(this::parseRecommendations)
                            .doOnNext(recommendations ->
                                    log.debug("Generated {} trending recommendations",
                                            recommendations.size()));
                })
                .onErrorResume(e -> {
                    log.error("Error generating trending SOPs", e);
                    return Mono.error(new RecommendationException("Failed to generate trending SOPs", e));
                });
    }

    private Mono<JsonNode> extractTokenPayload(String authToken) {
        try {
            String[] chunks = authToken.replace("Bearer ", "").split("\\.");
            if (chunks.length != 3) {
                return Mono.error(new RecommendationException("Invalid JWT token format", null));
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            JsonNode tokenData = objectMapper.readTree(payload);

            if (!tokenData.has("sub") || !tokenData.has("role") || !tokenData.has("departmentId")) {
                return Mono.error(new RecommendationException("Missing required claims in token", null));
            }

            return Mono.just(tokenData);
        } catch (Exception e) {
            return Mono.error(new RecommendationException("Error processing token", e));
        }
    }

    private List<RecommendationDTO> parseRecommendations(String response) {
        try {
            JsonNode root;
            try {
                root = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                int jsonStart = response.indexOf("{");
                int jsonEnd = response.lastIndexOf("}");
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonPart = response.substring(jsonStart, jsonEnd + 1);
                    root = objectMapper.readTree(jsonPart);
                } else {
                    log.error("Failed to find valid JSON in response: {}", response);
                    return Collections.emptyList();
                }
            }

            JsonNode recommendationsNode = root.path("recommendations");
            if (!recommendationsNode.isArray()) {
                log.error("No recommendations array found in response: {}", response);
                return Collections.emptyList();
            }

            return StreamSupport.stream(recommendationsNode.spliterator(), false)
                    .map(node -> {
                        String title = node.path("title").asText("");
                        double score = node.path("score").asDouble(0.0);
                        String reason = node.path("reason").asText("No reason provided");

                        if (title.isEmpty()) {
                            log.warn("Empty title in recommendation");
                            return null;
                        }
                        if (score < 0 || score > 1) {
                            log.warn("Invalid score {} for recommendation {}", score, title);
                            return null;
                        }

                        return new RecommendationDTO(title, score, reason);
                    })
                    .filter(rec -> rec != null && rec.getScore() > 0.5)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse recommendations. Raw response: {}", response, e);
            return Collections.emptyList();
        }
    }

    private List<SimilarSopDTO> parseSimilarSops(String response) {
        try {
            JsonNode root;
            try {
                root = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                int jsonStart = response.indexOf("{");
                int jsonEnd = response.lastIndexOf("}");
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonPart = response.substring(jsonStart, jsonEnd + 1);
                    root = objectMapper.readTree(jsonPart);
                } else {
                    log.error("Failed to find valid JSON in response: {}", response);
                    return Collections.emptyList();
                }
            }

            JsonNode similarSopsNode = root.path("similarSops");
            if (!similarSopsNode.isArray()) {
                log.error("No similarSops array found in response: {}", response);
                return Collections.emptyList();
            }

            return StreamSupport.stream(similarSopsNode.spliterator(), false)
                    .map(node -> {
                        String title = node.path("title").asText("");
                        double score = node.path("similarityScore").asDouble(0.0);
                        String reason = node.path("reason").asText("No reason provided");

                        if (title.isEmpty()) {
                            log.warn("Empty title in similar SOP");
                            return null;
                        }
                        if (score < 0 || score > 1) {
                            log.warn("Invalid score {} for similar SOP {}", score, title);
                            return null;
                        }

                        return new SimilarSopDTO(title, score, reason);
                    })
                    .filter(sop -> sop != null && sop.getSimilarityScore() > 0.5)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse similar SOPs. Raw response: {}", response, e);
            return Collections.emptyList();
        }
    }

    private SopDetails findSourceSop(GetAllSopDetailsResponse allSops, String sopId) {
        return allSops.getSopDetailsList().stream()
                .filter(sop -> sop.getSopId().equals(sopId))
                .findFirst()
                .orElseThrow(() -> new RecommendationException("Source SOP not found: " + sopId, null));
    }
}