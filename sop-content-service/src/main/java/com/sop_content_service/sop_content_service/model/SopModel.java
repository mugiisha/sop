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

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title; // Title of the SOP

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 200, message = "New section content must not exceed 200 characters")
    private String newSection;

    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Code must only contain letters, numbers, underscores, or hyphens")
    @Size(max = 500, message = "Code must not exceed 50 characters")
    private String code;

    @URL(message = "Document URL must be a valid URL")
    private String documentUrl; // URL of the document

    @URL(message = "Image URL must be a valid URL")
    private String imageUrl; // URL of the image

    private String status = "Draft"; // Default status

    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Version must be a valid number")
    private String version = "1.0"; // Default version

    @NotBlank(message = "Visibility cannot be blank")
    @Pattern(regexp = "^(Public|Private)$", message = "Visibility must be either 'Public' or 'Private'")
    private String visibility; // Visibility (e.g., "Public" or "Private")

    @NotEmpty(message = "At least one author is required")
    private List<@NotBlank(message = "Author name cannot be blank") String> author; // List of authors

    private List<@NotBlank(message = "Reviewer name cannot be blank") String> reviewer; // List of reviewers

    private List<@NotBlank(message = "Approver name cannot be blank") String> approver; // List of approvers

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
