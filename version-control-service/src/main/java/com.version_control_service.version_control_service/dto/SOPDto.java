package com.version_control_service.version_control_service.dto;
import com.version_control_service.version_control_service.enums.SOPStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class SOPDto {
    private String id;
    private String title;
    private String visibility;
    private String category;
    private SOPStatus status;
    private UUID authorId;
    private UUID departmentId;
    private List<UUID> reviewers;
    private UUID approverId;

}