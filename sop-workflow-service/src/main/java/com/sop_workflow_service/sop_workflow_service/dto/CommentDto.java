package com.sop_workflow_service.sop_workflow_service.dto;
import lombok.Data;

@Data
public class CommentDto {
    private String commentId;
    private String stageId;
    private String userId;
    private String content;
}
