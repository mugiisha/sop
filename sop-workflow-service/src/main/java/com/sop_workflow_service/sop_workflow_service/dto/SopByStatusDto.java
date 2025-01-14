package com.sop_workflow_service.sop_workflow_service.dto;

import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SopByStatusDto {
    private String id;
    private String title;
    private SOPStatus status;
    private Date createdAt;
    private Date updatedAt;
}
