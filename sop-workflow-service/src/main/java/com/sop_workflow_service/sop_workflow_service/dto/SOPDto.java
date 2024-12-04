package com.sop_workflow_service.sop_workflow_service.dto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SOPDto {
    private String sopId;
    private String title;
    private String description;
    private String status;
    private String departmentId;
    private String visibility;
    private List<String> workflowStages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
