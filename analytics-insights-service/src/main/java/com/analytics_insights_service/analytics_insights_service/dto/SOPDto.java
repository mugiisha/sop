package com.analytics_insights_service.analytics_insights_service.dto;

import com.analytics_insights_service.analytics_insights_service.enums.SOPStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
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
    private UUID initiatedBy;

    @NotEmpty(message = "Category is required")
    private String category;

    private SOPStatus status;

    @NotEmpty(message = "At least one reviewer is required")
    private List<UUID> reviewers;

    @NotNull(message = "Approver is required")
    private UUID approverId;

    private Date createdAt;

    private Date updatedAt;
}







