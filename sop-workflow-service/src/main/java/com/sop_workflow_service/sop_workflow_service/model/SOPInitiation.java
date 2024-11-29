package com.sop_workflow_service.sop_workflow_service.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "sops")
public class SOPInitiation {
    @Id
    private String id;
    private String title;
    private String description;
    private String status; // DRAFT, INITIATED, etc.
    private String visibility; // DEPARTMENT, COMPANY_WIDE
    private String departmentId; // FK to Department
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
