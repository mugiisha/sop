package com.sop_content_service.sop_content_service.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Document(collection = "feedbacks") // Specifies the MongoDB collection name
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
public class FeedbackModel {

    @Id
    private String id; // MongoDB's unique identifier

    @NotBlank(message = "SOP ID cannot be blank")
    private String sopId; // ID of the associated SOP

    @NotNull(message = "User ID cannot be null")
    private String userId; // ObjectId referencing the user from the user database

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 1000, message = "Content must not exceed 1000 characters")
    private String content; // Feedback content

    @CreatedDate
    private Date timestamp; // Automatically populated with the creation timestamp

    // Constructor with all parameters
    public FeedbackModel(String sopId, String userId, String content) {
        this.sopId = sopId;
        this.userId = userId;
        this.content = content;
    }
}
