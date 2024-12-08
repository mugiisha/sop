package com.sop_workflow_service.sop_workflow_service.dto;

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
    private String status;
    private List<String> comments;
}