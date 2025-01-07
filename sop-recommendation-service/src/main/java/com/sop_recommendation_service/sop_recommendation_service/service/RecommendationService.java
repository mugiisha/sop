package com.sop_recommendation_service.sop_recommendation_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_recommendation_service.sop_recommendation_service.dtos.*;
import com.sop_recommendation_service.sop_recommendation_service.exceptions.RecommendationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import sopFromWorkflow.GetAllSopDetailsResponse;
import sopFromWorkflow.SopDetails;
import userService.getUserInfoResponse;

import java.util.*;
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

    public Mono<RecommendationResponse> getPersonalizedRecommendations(String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.zip(
                        Mono.just(tokenData),
                        Mono.fromCallable(() -> userClientService.getUserInfo(tokenData.get("sub").asText())),
                        Mono.fromCallable(() -> sopClientService.getAllSopDetails())
                ))
                .flatMap(tuple -> {
                    JsonNode tokenData = tuple.getT1();
                    getUserInfoResponse userInfo = tuple.getT2();
                    GetAllSopDetailsResponse sopDetails = tuple.getT3();

                    if (sopDetails == null || sopDetails.getSopDetailsList() == null ||
                            sopDetails.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for recommendation generation");
                    }

                    String prompt = geminiClientService.buildPersonalizedPrompt(
                            tokenData.get("role").asText(),
                            tokenData.get("departmentId").asText(),
                            sopDetails.getSopDetailsList()
                    );
                    return geminiClientService.generateRecommendations(prompt)
                            .map(response -> parseRecommendations(response, sopDetails.getSopDetailsList()))
                            .map(recommendations -> createSuccessResponse(
                                    recommendations,
                                    "personalized",
                                    "Successfully generated recommendations"
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error generating personalized recommendations", e);
                    return createErrorResponse("Failed to generate personalized recommendations");
                });
    }

    public Mono<RecommendationResponse> getSimilarSOPs(String sopId, String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> sopClientService.getAllSopDetails())
                        .map(allSops -> Tuples.of(tokenData, allSops)))
                .flatMap(tuple -> {
                    GetAllSopDetailsResponse allSops = tuple.getT2();

                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for similarity comparison");
                    }

                    SopDetails sourceSop = findSourceSop(allSops, sopId);
                    String prompt = geminiClientService.buildSimilarityPrompt(
                            sourceSop,
                            allSops.getSopDetailsList()
                    );
                    return geminiClientService.generateRecommendations(prompt)
                            .map(response -> parseSimilarSops(response, allSops.getSopDetailsList()))
                            .map(recommendations -> createSuccessResponse(
                                    recommendations,
                                    "similar",
                                    "Successfully found similar SOPs"
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error finding similar SOPs", e);
                    return createErrorResponse("Failed to find similar SOPs");
                });
    }

    public Mono<RecommendationResponse> getDepartmentRecommendations(String departmentId, String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> sopClientService.getAllSopDetails()))
                .flatMap(allSops -> {
                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for department recommendations");
                    }

                    List<SopDetails> departmentSops = allSops.getSopDetailsList().stream()
                            .filter(sop -> sop.getDepartmentId().equals(departmentId))
                            .collect(Collectors.toList());

                    if (departmentSops.isEmpty()) {
                        return createEmptyResponse("No SOPs found for the specified department");
                    }

                    String prompt = geminiClientService.buildDepartmentPrompt(departmentId, departmentSops);
                    return geminiClientService.generateRecommendations(prompt)
                            .map(response -> parseRecommendations(response, departmentSops))
                            .map(recommendations -> createSuccessResponse(
                                    recommendations,
                                    "department",
                                    "Successfully generated department recommendations"
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error generating department recommendations", e);
                    return createErrorResponse("Failed to generate department recommendations");
                });
    }

    public Mono<RecommendationResponse> getTrendingSOPs(String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> sopClientService.getAllSopDetails()))
                .flatMap(allSops -> {
                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for trending recommendations");
                    }

                    String prompt = geminiClientService.buildTrendingPrompt(allSops.getSopDetailsList());
                    return geminiClientService.generateRecommendations(prompt)
                            .map(response -> parseRecommendations(response, allSops.getSopDetailsList()))
                            .map(recommendations -> createSuccessResponse(
                                    recommendations,
                                    "trending",
                                    "Successfully generated trending recommendations"
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error generating trending SOPs", e);
                    return createErrorResponse("Failed to generate trending SOPs");
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

    private List<RecommendationDTO> parseRecommendations(String response, List<SopDetails> existingSops) {
        try {
            JsonNode root = parseJsonResponse(response);
            JsonNode recommendationsNode = root.path("recommendations");

            if (!recommendationsNode.isArray()) {
                log.error("No recommendations array found in response: {}", response);
                return Collections.emptyList();
            }

            Map<String, SopDetails> sopsByTitle = existingSops.stream()
                    .collect(Collectors.toMap(
                            SopDetails::getTitle,
                            sop -> sop,
                            (sop1, sop2) -> sop1  // In case of duplicate titles, keep the first one
                    ));

            return StreamSupport.stream(recommendationsNode.spliterator(), false)
                    .map(node -> {
                        try {
                            String title = extractTitle(node);
                            if (title.isEmpty()) {
                                log.warn("Missing title in recommendation node: {}", node);
                                return null;
                            }

                            // Find matching existing SOP
                            SopDetails matchingSop = sopsByTitle.get(title);
                            if (matchingSop == null) {
                                log.warn("No matching SOP found for title: {}", title);
                                return null;
                            }

                            return RecommendationDTO.builder()
                                    .sopId(matchingSop.getSopId())  // Use real SOP ID
                                    .title(matchingSop.getTitle())
                                    .description(matchingSop.getDescription())
                                    .body(matchingSop.getBody())
                                    .documentUrls(matchingSop.getDocumentUrlsList())
                                    .category(matchingSop.getCategory())
                                    .departmentId(matchingSop.getDepartmentId())
                                    .status(matchingSop.getStatus())
                                    .visibility(matchingSop.getVisibility())
                                    .coverUrl(matchingSop.getCoverUrl())
//                                    .versions(parseVersions(matchingSop.getVersionsList()))
//                                    .reviewers(parseStages(matchingSop.getReviewersList()))
//                                    .approver(parseStage(matchingSop.getApprover()))
//                                    .author(parseStage(matchingSop.getAuthor()))
                                    .score(node.path("score").asDouble(0.75))
                                    .reason(node.path("reason").asText("No reason provided"))
                                    .createdAt(matchingSop.getCreatedAt())
                                    .updatedAt(matchingSop.getUpdatedAt())
                                    .build();
                        } catch (Exception e) {
                            log.error("Error processing recommendation node: {}", node, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse recommendations", e);
            return Collections.emptyList();
        }
    }

    private List<RecommendationDTO> parseSimilarSops(String response, List<SopDetails> existingSops) {
        try {
            JsonNode root = parseJsonResponse(response);
            JsonNode similarSopsNode = root.path("similarSops");

            if (!similarSopsNode.isArray()) {
                log.error("No similarSops array found in response: {}", response);
                return Collections.emptyList();
            }

            Map<String, SopDetails> sopsByTitle = existingSops.stream()
                    .collect(Collectors.toMap(
                            SopDetails::getTitle,
                            sop -> sop,
                            (sop1, sop2) -> sop1
                    ));

            return StreamSupport.stream(similarSopsNode.spliterator(), false)
                    .map(node -> {
                        try {
                            String title = extractTitle(node);
                            if (title.isEmpty()) {
                                log.warn("Missing title in similar SOP node: {}", node);
                                return null;
                            }

                            SopDetails matchingSop = sopsByTitle.get(title);
                            if (matchingSop == null) {
                                log.warn("No matching SOP found for title: {}", title);
                                return null;
                            }

                            return RecommendationDTO.builder()
                                    .sopId(matchingSop.getSopId())
                                    .title(matchingSop.getTitle())
                                    .description(matchingSop.getDescription())
                                    .body(matchingSop.getBody())
                                    .documentUrls(matchingSop.getDocumentUrlsList())
                                    .category(matchingSop.getCategory())
                                    .departmentId(matchingSop.getDepartmentId())
                                    .status(matchingSop.getStatus())
                                    .visibility(matchingSop.getVisibility())
                                    .coverUrl(matchingSop.getCoverUrl())
//                                    .versions(parseVersions(matchingSop.getVersionsList()))
//                                    .reviewers(parseStages(matchingSop.getReviewersList()))
//                                    .approver(parseStage(matchingSop.getApprover()))
//                                    .author(parseStage(matchingSop.getAuthor()))
                                    .score(node.path("similarityScore").asDouble(0.75))
                                    .reason(node.path("reason").asText("No reason provided"))
                                    .createdAt(matchingSop.getCreatedAt())
                                    .updatedAt(matchingSop.getUpdatedAt())
                                    .build();
                        } catch (Exception e) {
                            log.error("Error processing similar SOP node: {}", node, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse similar SOPs", e);
            return Collections.emptyList();
        }
    }

    private List<String> parseDocumentUrls(JsonNode node) {
        JsonNode urlsNode = node.path("documentUrls");
        if (urlsNode.isArray()) {
            return StreamSupport.stream(urlsNode.spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<SopVersion> parseVersions(List<sopFromWorkflow.SopVersion> versions) {
        return versions.stream()
                .map(version -> SopVersion.builder()
                        .versionNumber(version.getVersionNumber())
                        .currentVersion(version.getCurrentVersion())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Stage> parseStages(List<sopFromWorkflow.Stage> stages) {
        return stages.stream()
                .map(this::parseStage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Stage parseStage(sopFromWorkflow.Stage stage) {
        if (stage == null) {
            return null;
        }
        return Stage.builder()
                .name(stage.getName())
                .profilePictureUrl(stage.getProfilePictureUrl())
                .status(stage.getStatus())
//                .comments(parseComments(stage.getComments()))
                .build();
    }

    private List<Comment> parseComments(List<sopFromWorkflow.Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(comment -> Comment.builder()
                        .commentId(comment.getCommentId())
                        .comment(comment.getComment())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private JsonNode parseJsonResponse(String response) throws JsonProcessingException {
        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            // Try to extract JSON from text that might contain additional content
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonPart = response.substring(jsonStart, jsonEnd + 1);
                return objectMapper.readTree(jsonPart);
            }
            throw e;
        }
    }

    private String extractTitle(JsonNode node) {
        String title = node.path("title").asText("");
        if (title.isEmpty()) {
            title = node.path("name").asText("");
            if (title.isEmpty()) {
                title = node.path("sop_title").asText("");
            }
        }
        return title;
    }

    private SopDetails findSourceSop(GetAllSopDetailsResponse allSops, String sopId) {
        return allSops.getSopDetailsList().stream()
                .filter(sop -> sop.getSopId().equals(sopId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Source SOP not found with ID: {}", sopId);
                    return new RecommendationException("Source SOP not found: " + sopId, null);
                });
    }

    private Mono<RecommendationResponse> createEmptyResponse(String message) {
        return Mono.just(RecommendationResponse.builder()
                .message(message)
                .data(RecommendationData.builder()
                        .recommendations(Collections.emptyList())
                        .metadata(RecommendationMetadata.builder()
                                .requestType("empty")
                                .timestamp(System.currentTimeMillis())
                                .resultCount(0)
                                .build())
                        .build())
                .build());
    }

    private RecommendationResponse createSuccessResponse(
            List<RecommendationDTO> recommendations,
            String requestType,
            String message) {
        return RecommendationResponse.builder()
                .message(message)
                .data(RecommendationData.builder()
                        .recommendations(recommendations)
                        .metadata(RecommendationMetadata.builder()
                                .requestType(requestType)
                                .timestamp(System.currentTimeMillis())
                                .resultCount(recommendations.size())
                                .build())
                        .build())
                .build();
    }

    private Mono<RecommendationResponse> createErrorResponse(String errorMessage) {
        return Mono.just(RecommendationResponse.builder()
                .message(errorMessage)
                .data(RecommendationData.builder()
                        .recommendations(Collections.emptyList())
                        .metadata(RecommendationMetadata.builder()
                                .requestType("error")
                                .timestamp(System.currentTimeMillis())
                                .resultCount(0)
                                .build())
                        .build())
                .build());
    }
}