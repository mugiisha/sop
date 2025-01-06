package com.sop_content_service.sop_content_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarSopDTO {
    private String sopId;
    private String title;
    private String description;
    private String category;
    private String departmentId;
    private String status;
    private String visibility;
    private String coverUrl;
    private double similarityScore;
    private String reason;
    private String createdAt;
    private String updatedAt;
}
