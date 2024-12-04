package com.sop_workflow_service.sop_workflow_service.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "workflowStages")
public class WorkflowStage {
    @Id
    private String stageId;
    private String sopId;
    private String name;
    private String roleRequired; // Reviewer or Approver
    private String assignedUser;
    private String approvalStatus; // Pending, Reviewed, Needs Correction, Approved, Rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
