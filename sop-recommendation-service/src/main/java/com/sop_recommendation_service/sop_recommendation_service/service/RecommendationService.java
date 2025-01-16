package com.sop_recommendation_service.sop_recommendation_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_recommendation_service.sop_recommendation_service.dtos.*;
import com.sop_recommendation_service.sop_recommendation_service.exceptions.RecommendationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sopFromWorkflow.GetAllSopDetailsResponse;
import sopFromWorkflow.GetSopDetailsResponse;
import sopFromWorkflow.SopDetails;
import userService.getUserInfoResponse;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final GeminiRecommendationEngine recommendationEngine;
    private final SopClientService sopClientService;
    private final UserInfoClientService userClientService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SOP_CACHE_KEY = "sop:all";
    private static final String USER_CACHE_KEY = "user:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public Mono<RecommendationResponse> getPersonalizedRecommendations(String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> getRecommendationContext(tokenData))
                .flatMap(this::generateRecommendations)
                .onErrorResume(this::handleRecommendationError);
    }

    public Mono<RecommendationResponse> getKeywordBasedRecommendations(String authToken, String keywords) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> getRecommendationContext(tokenData))
                .flatMap(context -> generateKeywordRecommendations(context, keywords))
                .onErrorResume(this::handleRecommendationError);
    }

    private Mono<RecommendationContext> getRecommendationContext(JsonNode tokenData) {
        return Mono.zip(
                Mono.just(tokenData),
                getCachedUserInfo(tokenData.get("sub").asText()),
                getCachedSopDetails()
        ).map(tuple -> new RecommendationContext(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private Mono<RecommendationResponse> generateRecommendations(RecommendationContext context) {
        if (context.sopDetails() == null || context.sopDetails().getSopDetailsList() == null ||
                context.sopDetails().getSopDetailsList().isEmpty()) {
            return createEmptyResponse("No SOPs found for recommendation generation");
        }

        try {
            // Only get published SOPs
            List<SopDetails> publishedSops = context.sopDetails().getSopDetailsList().stream()
                    .filter(sop -> "PUBLISHED".equalsIgnoreCase(sop.getStatus()))
                    .collect(Collectors.toList());

            if (publishedSops.isEmpty()) {
                return createEmptyResponse("No published SOPs found for recommendation generation");
            }

            List<RecommendationResult> recommendations = recommendationEngine.generatePersonalizedRecommendations(
                    context.tokenData().get("role").asText(),
                    context.tokenData().get("departmentId").asText(),
                    publishedSops
            );

            return Mono.just(createSuccessResponse(
                    convertToRecommendationDTOs(recommendations, publishedSops),
                    "Successfully generated AI-powered recommendations"
            ));
        } catch (Exception e) {
            log.error("Error generating recommendations with AI", e);
            return Mono.error(new RecommendationException("Failed to generate AI recommendations"));
        }
    }

    private Mono<RecommendationResponse> generateKeywordRecommendations(RecommendationContext context, String keywords) {
        if (context.sopDetails() == null || context.sopDetails().getSopDetailsList() == null ||
                context.sopDetails().getSopDetailsList().isEmpty()) {
            return createEmptyResponse("No SOPs found for keyword-based recommendation generation");
        }

        try {
            // Only get published SOPs
            List<SopDetails> publishedSops = context.sopDetails().getSopDetailsList().stream()
                    .filter(sop -> "PUBLISHED".equalsIgnoreCase(sop.getStatus()))
                    .collect(Collectors.toList());

            if (publishedSops.isEmpty()) {
                return createEmptyResponse("No published SOPs found for keyword-based recommendation generation");
            }

            List<RecommendationResult> recommendations = recommendationEngine.generateKeywordBasedRecommendations(
                    keywords,
                    publishedSops
            );

            return Mono.just(createSuccessResponse(
                    convertToRecommendationDTOs(recommendations, publishedSops),
                    "Successfully generated keyword-based recommendations"
            ));
        } catch (Exception e) {
            log.error("Error generating keyword-based recommendations", e);
            return Mono.error(new RecommendationException("Failed to generate keyword-based recommendations"));
        }
    }

    private Mono<RecommendationResponse> handleRecommendationError(Throwable error) {
        log.error("Error in recommendation process", error);
        String errorMessage = error instanceof RecommendationException ?
                error.getMessage() :
                "Failed to generate recommendations: " + error.getMessage();
        return createErrorResponse(errorMessage);
    }

    private Mono<GetAllSopDetailsResponse> getCachedSopDetails() {
        return Mono.fromCallable(() -> {
            Object cached = redisTemplate.opsForValue().get(SOP_CACHE_KEY);
            if (cached != null) {
                log.debug("Cache hit for SOPs");
                return (GetAllSopDetailsResponse) cached;
            }

            log.debug("Cache miss for SOPs, fetching from service");
            GetAllSopDetailsResponse sopDetails = sopClientService.getAllSopDetails();
            if (sopDetails != null) {
                redisTemplate.opsForValue().set(SOP_CACHE_KEY, sopDetails, CACHE_TTL);
            }
            return sopDetails;
        });
    }

    private Mono<getUserInfoResponse> getCachedUserInfo(String userId) {
        String userCacheKey = USER_CACHE_KEY + userId;
        return Mono.fromCallable(() -> {
            Object cached = redisTemplate.opsForValue().get(userCacheKey);
            if (cached != null) {
                log.debug("Cache hit for user info: {}", userId);
                return (getUserInfoResponse) cached;
            }

            log.debug("Cache miss for user info: {}, fetching from service", userId);
            getUserInfoResponse userInfo = userClientService.getUserInfo(userId);
            if (userInfo != null) {
                redisTemplate.opsForValue().set(userCacheKey, userInfo, CACHE_TTL);
            }
            return userInfo;
        });
    }

    private Mono<JsonNode> extractTokenPayload(String authToken) {
        try {
            String[] chunks = authToken.replace("Bearer ", "").split("\\.");
            if (chunks.length != 3) {
                return Mono.error(new RecommendationException("Invalid JWT token format"));
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            JsonNode tokenData = objectMapper.readTree(payload);

            if (!tokenData.has("sub") || !tokenData.has("role") || !tokenData.has("departmentId")) {
                return Mono.error(new RecommendationException("Missing required claims in token"));
            }

            return Mono.just(tokenData);
        } catch (Exception e) {
            return Mono.error(new RecommendationException("Error processing token"));
        }
    }

    private List<RecommendationDTO> convertToRecommendationDTOs(
            List<RecommendationResult> results,
            List<SopDetails> allSops) {
        Map<String, SopDetails> sopsByTitle = new HashMap<>();
        for (SopDetails sop : allSops) {
            sopsByTitle.put(sop.getTitle(), sop);
        }

        List<RecommendationDTO> dtos = new ArrayList<>();
        for (RecommendationResult result : results) {
            SopDetails sop = sopsByTitle.get(result.getTitle());
            if (sop != null) {
                // Get full SOP details including versions, reviewers, etc.
                GetSopDetailsResponse fullDetails = sopClientService.getSopDetails(sop.getSopId());

                RecommendationDTO dto = RecommendationDTO.builder()
                        .sopId(sop.getSopId())
                        .documentUrls(sop.getDocumentUrlsList())
                        .coverUrl(sop.getCoverUrl())
                        .title(sop.getTitle())
                        .description(sop.getDescription())
                        .body(sop.getBody())
                        .category(sop.getCategory())
                        .departmentId(sop.getDepartmentId())
                        .visibility(sop.getVisibility())
                        .status(sop.getStatus())
                        .versions(convertVersions(fullDetails.getVersionsList()))
                        .reviewers(convertStages(fullDetails.getReviewersList()))
                        .approver(convertStage(fullDetails.getApprover()))
                        .author(convertStage(fullDetails.getAuthor()))
                        .score(result.getScore())
                        .reason(result.getReason())
                        .createdAt(sop.getCreatedAt())
                        .updatedAt(sop.getUpdatedAt())
                        .build();
                dtos.add(dto);
            }
        }
        return dtos;
    }

    private List<SopVersion> convertVersions(List<sopFromWorkflow.SopVersion> protoVersions) {
        return protoVersions.stream()
                .map(v -> SopVersion.builder()
                        .versionNumber(v.getVersionNumber())
                        .currentVersion(v.getCurrentVersion())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Stage> convertStages(List<sopFromWorkflow.Stage> protoStages) {
        return protoStages.stream()
                .map(this::convertStage)
                .collect(Collectors.toList());
    }

    private Stage convertStage(sopFromWorkflow.Stage protoStage) {
        if (protoStage == null) {
            return null;
        }

        return Stage.builder()
                .name(protoStage.getName())
                .profilePictureUrl(protoStage.getProfilePictureUrl())
                .status(protoStage.getStatus())
                .comments(protoStage.getCommentsList().stream()
                        .map(c -> Comment.builder()
                                .commentId(c.getCommentId())
                                .comment(c.getComment())
                                .createdAt(c.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private RecommendationResponse createSuccessResponse(
            List<RecommendationDTO> recommendations,
            String message) {
        return RecommendationResponse.builder()
                .message(message)
                .data(RecommendationData.builder()
                        .recommendations(recommendations)
                        .metadata(RecommendationMetadata.builder()
                                .requestType("personalized")
                                .timestamp(System.currentTimeMillis())
                                .resultCount(recommendations.size())
                                .build())
                        .build())
                .build();
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
    public Mono<RecommendationDTO> getRecommendedSop(String authToken, String sopId) {
        return extractTokenPayload(authToken)  // First extract and parse the token
                .flatMap(tokenData -> getRecommendationContext(tokenData))
                .flatMap(context -> {
                    try {
                        // Get published SOPs
                        List<SopDetails> publishedSops = context.sopDetails().getSopDetailsList().stream()
                                .filter(sop -> "PUBLISHED".equalsIgnoreCase(sop.getStatus()))
                                .collect(Collectors.toList());

                        if (publishedSops.isEmpty()) {
                            return Mono.error(new RecommendationException("No published SOPs found"));
                        }

                        // Find the specific SOP
                        Optional<SopDetails> requestedSop = publishedSops.stream()
                                .filter(sop -> sop.getSopId().equals(sopId))
                                .findFirst();

                        if (requestedSop.isEmpty()) {
                            return Mono.error(new RecommendationException("SOP not found"));
                        }

                        // Generate recommendations to get the score and reason
                        List<RecommendationResult> recommendations = recommendationEngine
                                .generatePersonalizedRecommendations(
                                        context.tokenData().get("role").asText(),
                                        context.tokenData().get("departmentId").asText(),
                                        Collections.singletonList(requestedSop.get())
                                );

                        if (recommendations.isEmpty()) {
                            return Mono.error(new RecommendationException("Failed to generate recommendation for SOP"));
                        }

                        // Convert to DTO
                        List<RecommendationDTO> dtos = convertToRecommendationDTOs(
                                recommendations,
                                Collections.singletonList(requestedSop.get())
                        );

                        if (dtos.isEmpty()) {
                            return Mono.error(new RecommendationException("Failed to process recommendation"));
                        }

                        return Mono.just(dtos.get(0));
                    } catch (Exception e) {
                        return Mono.error(new RecommendationException("Error retrieving recommended SOP: "
                                + e.getMessage()));
                    }
                });
    }
    private record RecommendationContext(
            JsonNode tokenData,
            getUserInfoResponse userInfo,
            GetAllSopDetailsResponse sopDetails
    ) {}
}