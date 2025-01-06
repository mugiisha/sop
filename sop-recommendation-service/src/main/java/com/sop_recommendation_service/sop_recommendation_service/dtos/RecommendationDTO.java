package com.sop_recommendation_service.sop_recommendation_service.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationDTO {
    @JsonProperty("id")
    private String sopId;

    @JsonProperty("documentUrls")
    private List<String> documentUrls;

    @JsonProperty("coverUrl")
    private String coverUrl;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("body")
    private String body;

    @JsonProperty("category")
    private String category;

    @JsonProperty("departmentId")
    private String departmentId;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("status")
    private String status;

    @JsonProperty("versions")
    private List<SopVersion> versions;

    @JsonProperty("reviewers")
    private List<Stage> reviewers;

    @JsonProperty("approver")
    private Stage approver;

    @JsonProperty("author")
    private Stage author;

    @JsonProperty("score")
    private Double score;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;
}