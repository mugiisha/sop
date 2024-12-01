package com.sop_workflow_service.sop_workflow_service.dto;
import com.sop_workflow_service.sop_workflow_service.dto.WorkflowInitiationDto;
import lombok.Data;

import java.util.List;

@Data
public class SOPInitiationDto {
    private String title; // SOP title
    private String description; // SOP description
    private String visibility; // DEPARTMENT, COMPANY_WIDE
    private String departmentId; // FK to Department
    private List<WorkflowInitiationDto> workflowStages; // List of workflow stages associated with the SOP
}
