package com.analytics_insights_service.analytics_insights_service.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;


import java.util.Date;

@Document(collection = "feedbacks") // Specifies the MongoDB collection name
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
public class FeedbackModel {

    @Id
    private String id; // MongoDB's unique identifier

    @NotBlank(message = "SOP ID cannot be blank")
    private String sopId; // ID of the associated SOP

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotNull(message = "User Name cannot be null")
    private String userName;

    @NotNull(message = "role cannot be null")
    private String role;

    @NotNull(message = "departmentName be null")
    private String departmentName;

    @NotNull(message = "profilePic cannot be null")
    private String profilePic;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 1000, message = "Content must not exceed 1000 characters")
    private String content; // Feedback content

    @CreatedDate
    private Date timestamp; // Automatically populated with the creation timestamp

    @Size(max = 1000, message = "Response must not exceed 1000 characters")
    private String response;

    // Constructor with all parameters
    public FeedbackModel(String sopId, String userName, String profilePic ,String role , String departmentName,String content, String response , String title) {
        this.sopId = sopId;
        this.userName = userName;
        this.profilePic = profilePic;
        this.content = content;
        this.response = response;
        this.title = title;
        this.role = role;
        this.departmentName = departmentName;

    }
}
