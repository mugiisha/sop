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
            List<RecommendationResult> recommendations = recommendationEngine.generatePersonalizedRecommendations(
                    context.tokenData().get("role").asText(),
                    context.tokenData().get("departmentId").asText(),
                    context.sopDetails().getSopDetailsList()
            );

            return Mono.just(createSuccessResponse(
                    convertToRecommendationDTOs(recommendations, context.sopDetails().getSopDetailsList()),
                    "Successfully generated AI-powered recommendations"
            ));
        } catch (Exception e) {
            log.error("Error generating recommendations with AI", e);
            return Mono.error(new RecommendationException("Failed to generate AI recommendations", e));
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
        Map<String, SopDetails> sopsByTitle = new HashMap<>();
        for (SopDetails sop : allSops) {
            sopsByTitle.put(sop.getTitle(), sop);
        }

        List<RecommendationDTO> dtos = new ArrayList<>();
        for (RecommendationResult result : results) {
            SopDetails sop = sopsByTitle.get(result.getTitle());
            if (sop != null) {
                dtos.add(RecommendationDTO.builder()
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
                        .build());
            }
        }
        return dtos;
    }

    private record RecommendationContext(
            JsonNode tokenData,
            getUserInfoResponse userInfo,
            GetAllSopDetailsResponse sopDetails
    ) {}

    private RecommendationResponse createSuccessResponse(
            List<RecommendationDTO> recommendations,
            String message) {
        return RecommendationResponse.builder()
                .message(message)
                .data(com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationData.builder()
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
                .data(com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationData.builder()
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
                .data(com.sop_recommendation_service.sop_recommendation_service.dtos.RecommendationData.builder()
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