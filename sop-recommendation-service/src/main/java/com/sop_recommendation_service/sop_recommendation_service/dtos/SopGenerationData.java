package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SopGenerationData {
    private String sopId;
    private String title;
    private String content;
    private String category;
    private String departmentId;
    private String status;
    private String createdBy;
    private long timestamp;
}
