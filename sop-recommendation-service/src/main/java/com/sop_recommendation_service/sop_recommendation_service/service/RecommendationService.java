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
import sopFromWorkflow.SopDetails;
import userService.getUserInfoResponse;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationEngine recommendationEngine;
    private final SopClientService sopClientService;
    private final UserInfoClientService userClientService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SOP_CACHE_KEY = "sop:all";
    private static final String USER_CACHE_KEY = "user:";
    private static final String SIMILAR_SOPS_CACHE_KEY = "similar:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public Mono<RecommendationResponse> getPersonalizedRecommendations(String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.zip(
                        Mono.just(tokenData),
                        getCachedUserInfo(tokenData.get("sub").asText()),
                        getCachedSopDetails()
                ))
                .flatMap(tuple -> {
                    JsonNode tokenData = tuple.getT1();
                    getUserInfoResponse userInfo = tuple.getT2();
                    GetAllSopDetailsResponse sopDetails = tuple.getT3();

                    if (sopDetails == null || sopDetails.getSopDetailsList() == null ||
                            sopDetails.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for recommendation generation");
                    }

                    List<RecommendationResult> recommendations = recommendationEngine.generatePersonalizedRecommendations(
                            tokenData.get("role").asText(),
                            tokenData.get("departmentId").asText(),
                            sopDetails.getSopDetailsList()
                    );

                    return Mono.just(createSuccessResponse(
                            convertToRecommendationDTOs(recommendations, sopDetails.getSopDetailsList()),
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
        String similarSopsCacheKey = SIMILAR_SOPS_CACHE_KEY + sopId;
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> Mono.fromCallable(() -> {
                    // Check cache first
                    Object cached = redisTemplate.opsForValue().get(similarSopsCacheKey);
                    if (cached != null) {
                        log.debug("Cache hit for similar SOPs: {}", sopId);
                        return (RecommendationResponse) cached;
                    }

                    GetAllSopDetailsResponse allSops = getCachedSopDetails().block();
                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for similarity comparison").block();
                    }

                    SopDetails sourceSop = findSourceSop(allSops, sopId);
                    List<RecommendationResult> recommendations = recommendationEngine.findSimilarSops(
                            sourceSop,
                            allSops.getSopDetailsList()
                    );

                    RecommendationResponse response = createSuccessResponse(
                            convertToRecommendationDTOs(recommendations, allSops.getSopDetailsList()),
                            "similar",
                            "Successfully found similar SOPs"
                    );

                    // Cache the response
                    redisTemplate.opsForValue().set(similarSopsCacheKey, response, CACHE_TTL);
                    return response;
                }))
                .onErrorResume(e -> {
                    log.error("Error finding similar SOPs", e);
                    return createErrorResponse("Failed to find similar SOPs");
                });
    }

    public Mono<RecommendationResponse> getDepartmentRecommendations(String departmentId, String authToken) {
        return extractTokenPayload(authToken)
                .flatMap(tokenData -> getCachedSopDetails())
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

                    List<RecommendationResult> recommendations = recommendationEngine.generateDepartmentRecommendations(
                            departmentId,
                            departmentSops
                    );

                    return Mono.just(createSuccessResponse(
                            convertToRecommendationDTOs(recommendations, departmentSops),
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
                .flatMap(tokenData -> getCachedSopDetails())
                .flatMap(allSops -> {
                    if (allSops == null || allSops.getSopDetailsList() == null ||
                            allSops.getSopDetailsList().isEmpty()) {
                        return createEmptyResponse("No SOPs found for trending recommendations");
                    }

                    List<RecommendationResult> recommendations = recommendationEngine.getTrendingSops(
                            allSops.getSopDetailsList()
                    );

                    return Mono.just(createSuccessResponse(
                            convertToRecommendationDTOs(recommendations, allSops.getSopDetailsList()),
                            "trending",
                            "Successfully generated trending recommendations"
                    ));
                })
                .onErrorResume(e -> {
                    log.error("Error generating trending SOPs", e);
                    return createErrorResponse("Failed to generate trending SOPs");
                });
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

    private SopDetails findSourceSop(GetAllSopDetailsResponse allSops, String sopId) {
        return allSops.getSopDetailsList().stream()
                .filter(sop -> sop.getSopId().equals(sopId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Source SOP not found with ID: {}", sopId);
                    return new RecommendationException("Source SOP not found: " + sopId, null);
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

    private List<RecommendationDTO> convertToRecommendationDTOs(
            List<RecommendationResult> results,
            List<SopDetails> allSops) {
        Map<String, SopDetails> sopsByTitle = allSops.stream()
                .collect(Collectors.toMap(
                        SopDetails::getTitle,
                        sop -> sop,
                        (sop1, sop2) -> sop1
                ));

        return results.stream()
                .map(result -> {
                    SopDetails sop = sopsByTitle.get(result.getTitle());
                    if (sop == null) {
                        return null;
                    }

                    return RecommendationDTO.builder()
                            .sopId(sop.getSopId())
                            .title(sop.getTitle())
                            .description(sop.getDescription())
                            .body(sop.getBody())
                            .documentUrls(sop.getDocumentUrlsList())
                            .category(sop.getCategory())
                            .departmentId(sop.getDepartmentId())
                            .status(sop.getStatus())
                            .visibility(sop.getVisibility())
                            .coverUrl(sop.getCoverUrl())
                            .score(result.getScore())
                            .reason(result.getReason())
                            .createdAt(sop.getCreatedAt())
                            .updatedAt(sop.getUpdatedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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