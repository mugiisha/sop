package com.sop_workflow_service.sop_workflow_service.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "sops")
public class SOP {
    @Id
    private String sopId;
    private String title;
    private String description;
    private String status; // Draft, In Review, Needs Correction, Ready for Approval, In Approval, Approved, Rejected
    private String departmentId;
    private String visibility;
    private List<WorkflowStage> workflowStages; // Store full WorkflowStage objects here
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
