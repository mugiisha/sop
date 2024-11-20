package com.sop_workflow_service.sop_workflow_service.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "sops")
public class SOP {

    @Id
    private String id;

    private String title;
    private String description;
    private String version;
    private String status; // Example: DRAFT, REVIEW, APPROVAL

    @DBRef
    private List<Author> author; // References to authors

    @DBRef
    private List<Reviewer> reviewers; // References to reviewers

    @DBRef
    private List<Approver> approvers; // References to approvers
}
