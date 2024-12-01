package com.version_control_service.version_control_service.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "sop_versions") // MongoDB collection name
@Data // Lombok annotation for getters, setters, toString, etc.
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
public class SopVersionModel {

    @Id
    private String id;

    @NotBlank(message = "SOP ID cannot be blank")
    @Size(max = 50, message = "SOP ID must not exceed 50 characters")
    private ObjectId sopId;  // ID of the associated SOP

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title; // Title of the versioned SOP

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description; // Description of the versioned SOP

    @NotBlank(message = "Version number cannot be blank")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)*$", message = "Version number must follow a valid format, e.g., '1.0' or '1.1.2'")
    @Size(max = 20, message = "Version number must not exceed 20 characters")
    private String versionNumber; // Version identifier

    @NotBlank(message = "Created by cannot be blank")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy; // User who created this version

    @NotBlank(message = "Code cannot be blank")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Code must only contain letters, numbers, underscores, or hyphens")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code; // Unique code for the SOP version

    @NotBlank(message = "Visibility cannot be blank")
    @Pattern(regexp = "^(Public|Private)$", message = "Visibility must be either 'Public' or 'Private'")
    private String visibility; // Visibility of the version (e.g., Public/Private)

    @NotBlank(message = "Category cannot be blank")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category; // Category of the SOP

    @URL(message = "Image URL must be a valid URL")
    private String imageFile; // URL pointing to the associated image

    @URL(message = "File URL must be a valid URL")
    private String documentFile; // URL pointing to the associated file

    @CreatedDate
    @Field("createdAt") // Maps to MongoDB's `createdAt` field
    private Date createdAt; // Timestamp of when this version was created

    // Constructor with all fields
    public SopVersionModel(
            ObjectId sopId,
            String title,
            String description,
            String versionNumber,
            String createdBy,
            String code,
            String visibility,
            String category,
            String imageFile,
            String documentFile
    ) {
        this.sopId = sopId;
        this.title = title;
        this.description = description;
        this.versionNumber = versionNumber;
        this.createdBy = createdBy;
        this.code = code;
        this.visibility = visibility;
        this.category = category;
        this.imageFile = imageFile;
        this.documentFile = documentFile;
    }
}
