package com.sop_workflow_service.sop_workflow_service.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "reviewers") // MongoDB collection name
public class Reviewer {

    @Id
    private String id; // MongoDB ObjectId as a String

    private String name;
    private String email;


}
