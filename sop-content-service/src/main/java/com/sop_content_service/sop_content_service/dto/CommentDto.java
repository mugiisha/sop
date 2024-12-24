package com.sop_content_service.sop_content_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    String commentId;
    String comment;
    LocalDateTime createdAt;
}
