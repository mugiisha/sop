package com.sop_workflow_service.sop_workflow_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CommentDto {
    @NotEmpty(message = "Comment is required")
    String comment;
}
