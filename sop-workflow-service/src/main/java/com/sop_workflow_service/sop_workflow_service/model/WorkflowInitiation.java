package com.sop_workflow_service.sop_workflow_service.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "workflow_stages")
public class WorkflowInitiation {
    @Id
    private String id;
    private String sopId; // FK to SOP
    private String name; // Stage name
    private String roleRequired; // Role needed for the stage
    private String assignedUser; // User assigned to the stage
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private Integer sequenceNumber; // Order in the workflow
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
