package com.sop_content_service.sop_content_service.dto;

import com.sop_content_service.sop_content_service.enums.SOPStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SOPDto {
    private String id;
    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Visibility is required")
    private String visibility;

    @NotNull(message = "Author ID is required")
    private UUID authorId;
    private UUID departmentId;

    @NotEmpty(message = "Category is required")
    private String category;

    private SOPStatus status;

    @NotEmpty(message = "At least one reviewer is required")
    private List<UUID> reviewers;

    @NotNull(message = "Approver is required")
    private UUID approverId;
}







