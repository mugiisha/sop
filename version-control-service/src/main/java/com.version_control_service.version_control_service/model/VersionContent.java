package com.version_control_service.version_control_service.model;

import com.version_control_service.version_control_service.enums.Visibility;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Document(collection = "version_content")
public class VersionContent {
    @Id
    private String id;
    private List<String> documentUrls;
    private String coverUrl;
    private String title;
    private String description;
    private String body;
    private Visibility visibility;
    private String category;
    private UUID departmentId;
    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
}
