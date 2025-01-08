package com.sop_workflow_service.sop_workflow_service.model;
import com.sop_workflow_service.sop_workflow_service.enums.Roles;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "workflow-stage")
public class WorkflowStage {
    @Id
    private String id;
    private String sopId;
    // roleRequired value set to enum of role required in the approval stage
    private Roles roleRequired;
    private UUID userId;
    private ApprovalStatus approvalStatus;
    @DBRef
    private List<Comment> comments;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
