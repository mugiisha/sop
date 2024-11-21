package com.sop_workflow_service.sop_workflow_service.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_approve") // MongoDB collection name
public class User {

    @Id
    private String id; // MongoDB ObjectId as a String

    private String name;
    private String email;
    private String role; // AUTHOR, REVIEWER, APPROVER
}
