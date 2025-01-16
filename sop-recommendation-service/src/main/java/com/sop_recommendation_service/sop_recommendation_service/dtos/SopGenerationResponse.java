package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SopGenerationResponse {
    private String message;
    private AiResponse data;
}