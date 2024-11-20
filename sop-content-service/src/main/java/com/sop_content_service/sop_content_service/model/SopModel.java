package com.sop_content_service.sop_content_service.model;

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

    private String title; // Title of the SOP
    private String description;
    private String newSection;
    private String code;
    private String documentUrl; // URL of the document
    private String imageUrl; // URL of the image
    private String status = "Draft"; // Default status
    private String version = "1.0"; // Default version
    private String visibility; // Visibility (e.g., "Public" or "Private")

    private List<String> author; // List of authors
    private List<String> reviewer; // List of reviewers
    private List<String> approver; // List of approvers

    @CreatedDate
    private Date createdAt;  // Automatically populated with the creation timestamp

    @LastModifiedDate
    private Date updatedAt; // Automatically updated when the document changes

    // Constructor with all parameters
    public SopModel(String title, String visibility, List<String> author, List<String> reviewer, List<String> approver) {
        this.title = title;
        this.visibility = visibility;
        this.author = author;
        this.reviewer = reviewer;
        this.approver = approver;
    }
}
