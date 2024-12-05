package com.sop_content_service.sop_content_service.model;

import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
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

    @NotEmpty(message = "At least one document URL is required")
    private List<@URL(message = "Each document URL must be valid") String> documentUrls; // List of document URLs

    @URL(message = "Cover URL must be a valid URL")
    private String coverUrl;

    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Description is required")
    private String description;

    @NotEmpty(message = "Body is required")
    private String body;

    @NotEmpty(message = "Category is required")
    private String category;

    @NotEmpty(message = "Visibility is required")
    private String visibility;

    @NotEmpty(message = "Authors list is required")
    private List<String> authors;

    @NotEmpty(message = "Reviewers list is required")
    private List<String> reviewers;

    @NotEmpty(message = "Approvers list is required")
    private List<String> approvers;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    // Constructor for the required fields
    public SopModel(List<String> documentUrls, String coverUrl, String title, String description, String body, String category, String visibility, List<String> authors, List<String> reviewers, List<String> approvers) {
        this.documentUrls = documentUrls;
        this.coverUrl = coverUrl;
        this.title = title;
        this.description = description;
        this.body = body;
        this.category = category;
        this.visibility = visibility;
        this.authors = authors;
        this.reviewers = reviewers;
        this.approvers = approvers;
    }

    // Optional constructor for when only document URLs and cover URL are needed
    public SopModel(List<String> documentUrls, String coverUrl) {
        this.documentUrls = documentUrls;
        this.coverUrl = coverUrl;
    }
}
