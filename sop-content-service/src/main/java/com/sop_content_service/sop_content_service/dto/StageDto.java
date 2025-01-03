package com.sop_content_service.sop_content_service.dto;

import com.sop_content_service.sop_content_service.enums.ApprovalStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StageDto {
    private UUID userId;
    private String name;
    private String profilePictureUrl;
    private ApprovalStatus status;
    private List<CommentDto> comments;
}