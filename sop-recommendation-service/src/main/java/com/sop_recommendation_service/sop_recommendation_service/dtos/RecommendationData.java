package com.sop_recommendation_service.sop_recommendation_service.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationData {
    @JsonProperty("recommendations")
    private List<RecommendationDTO> recommendations;

    @JsonProperty("metadata")
    private RecommendationMetadata metadata;
}

