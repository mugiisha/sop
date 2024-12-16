package com.sop_workflow_service.sop_workflow_service.dto;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Category;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class SOPDto {
    private String id;
    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Visibility is required")
    private String visibility;

    @NotEmpty(message = "Category is required")
    private String category;

    private SOPStatus status = SOPStatus.INITIALIZED;

    @NotNull(message = "Author ID is required")
    private UUID authorId;
    private UUID departmentId;

    @NotEmpty(message = "At least one reviewer is required")
    private List<UUID> reviewers;

    @NotNull(message = "Approver is required")
    private UUID approverId;

}