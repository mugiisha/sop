package com.sop_content_service.sop_content_service.model;

import com.sop_content_service.sop_content_service.enums.SOPStatus;
import com.sop_content_service.sop_content_service.enums.Visibility;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "sops") // Specifies the MongoDB collection name
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
public class Sop {

    @Id
    private String id;
    private List<String> documentUrls;
    private String coverUrl;
    private String title;
    private String description;
    private String body;
    private String category;
    private UUID departmentId;
    private Visibility visibility;
    private SOPStatus status;
    private UUID author;
    private List<UUID> reviewers;
    private UUID approver;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

}
