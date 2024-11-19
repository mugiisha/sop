package com.sop_content_service.sop_content_service.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.util.Date;

@Document(collection = "sops") // Specifies the MongoDB collection name
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
public class SopModel {
    @Id
    private String id; // MongoDB's unique identifier

    private String sopId;
    private String description;
    private String newSection;
    private String code;
    private String documentUrl; // URL of the document
    private String imageUrl; // URL of the image
    private String status = "Draft"; // Default status
    private String version = "1.0"; // Default version

    @CreatedDate
    private Date createdAt;  // Automatically populated with the creation timestamp

    @LastModifiedDate
    private Date updatedAt;

    // Constructor with parameters
    public SopModel(String sopId, String description, String newSection, String code, String documentUrl, String imageUrl) {
        this.sopId = sopId;
        this.description = description;
        this.newSection = newSection;
        this.code = code;
        this.documentUrl = documentUrl;
        this.imageUrl = imageUrl;
        this.status = "Draft"; // Default value
        this.version = "1.0"; // Default value
    }

}
