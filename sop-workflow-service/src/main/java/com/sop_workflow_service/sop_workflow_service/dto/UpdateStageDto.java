package com.sop_workflow_service.sop_workflow_service.dto;

import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateStageDto {
    ApprovalStatus approvalStatus;
    String comment;
}
