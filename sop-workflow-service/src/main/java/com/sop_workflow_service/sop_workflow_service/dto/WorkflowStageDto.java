package com.sop_workflow_service.sop_workflow_service.dto;
import lombok.Data;

@Data
public class WorkflowStageDto {
    private String stageId;
    private String sopId;
    private String name;
    private String roleRequired;
    private String assignedUser;
    private String approvalStatus;
}
