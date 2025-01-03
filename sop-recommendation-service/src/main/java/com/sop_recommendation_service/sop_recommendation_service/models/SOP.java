package com.sop_recommendation_service.sop_recommendation_service.models;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "sops")
public class SOP {
    @Id
    private String id;
    private String title;
    private String content;
    private String department;
    private String category;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private List<String> tags;
    private Integer viewCount;
}