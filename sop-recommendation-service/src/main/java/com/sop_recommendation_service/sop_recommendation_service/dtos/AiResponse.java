package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiResponse {
    private String content;
    private long timestamp;
}