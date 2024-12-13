package com.sop_workflow_service.sop_workflow_service.model;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Visibility;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sop")
public class SOP {
    @Id
    private String id;
    private String title;
    private SOPStatus status;
    private UUID departmentId;
    private Visibility visibility;
    @DBRef
    private Category category;
    @DBRef
    private List<WorkflowStage> workflowStages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
