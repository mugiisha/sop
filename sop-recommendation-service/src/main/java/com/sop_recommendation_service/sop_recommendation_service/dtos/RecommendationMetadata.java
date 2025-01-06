package com.sop_recommendation_service.sop_recommendation_service.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationMetadata {
    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("resultCount")
    private Integer resultCount;
}

