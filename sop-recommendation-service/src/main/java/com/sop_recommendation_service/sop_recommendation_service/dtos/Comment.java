package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String commentId;
    private String comment;
    private String createdAt;
}

