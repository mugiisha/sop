package com.sop_content_service.sop_content_service.model;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Document(collection = "sops_model") // Specifies the MongoDB collection name
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
public class SopModel {

    @Id
    private String id; // MongoDB's unique identifier

    private List<String> documentUrls; // List of document URLs

    private String coverUrl;

    private String title;

    private String description;

    private String body;

    private String categoryId;

    private String visibility;

    private List<String> authors;

    private List<String> reviewers;

    private List<String> approvers;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

}
