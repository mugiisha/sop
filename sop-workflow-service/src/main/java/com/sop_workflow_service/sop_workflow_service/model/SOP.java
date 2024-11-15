package com.sop_workflow_service.sop_workflow_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SOP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    private Long authorId;

    @ElementCollection
    private List<Long> reviewerIds;

    private Long approverId;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Visibility {
        DEPARTMENT, ORGANIZATION
    }
}
