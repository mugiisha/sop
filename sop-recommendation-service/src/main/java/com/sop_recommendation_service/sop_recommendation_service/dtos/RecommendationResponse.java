package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private RecommendationData data;
}
