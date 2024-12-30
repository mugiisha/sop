package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.Data;

@Data
public class SOPGenerationResponseDTO {
    private String id;
    private String title;
    private String content;
    private String status;
    private String message;
}
