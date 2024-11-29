package com.sop_workflow_service.sop_workflow_service.dto;
import lombok.Data;

@Data
public class WorkflowInitiationDto {
    private String name; // Stage name
    private String roleRequired; // Role needed for the stage
    private String assignedUser; // User assigned to the stage
    private Integer sequenceNumber; // Order in the workflow
    private String approvalStatus; // PENDING, APPROVED, REJECTED (optional, default PENDING)
}
