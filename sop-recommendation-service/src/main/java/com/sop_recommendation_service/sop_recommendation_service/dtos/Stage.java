package com.sop_recommendation_service.sop_recommendation_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stage {
    private String name;
    private String profilePictureUrl;
    private String status;
    private List<Comment> comments;
}

