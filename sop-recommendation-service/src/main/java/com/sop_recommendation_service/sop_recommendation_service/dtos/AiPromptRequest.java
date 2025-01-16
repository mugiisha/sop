package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.Data;

@Data
public class AiPromptRequest {
    private String prompt;
    private String type; // "SOP" or "GENERAL"
}