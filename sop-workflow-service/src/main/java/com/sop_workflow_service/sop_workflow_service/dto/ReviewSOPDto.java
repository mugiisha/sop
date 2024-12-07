package com.sop_workflow_service.sop_workflow_service.dto;

import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewSOPDto {
    @NotNull(message = "Approval status is required")
    ApprovalStatus approvalStatus;
    String comment;
}
