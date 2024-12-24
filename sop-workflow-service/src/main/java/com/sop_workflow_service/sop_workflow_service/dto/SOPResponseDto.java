package com.sop_workflow_service.sop_workflow_service.dto;

import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SOPResponseDto {
        private String id;
        private String title;
        private SOPStatus status;
        private String category;
        private List<StageDto> reviewers;
        private StageDto approver;
        private StageDto author;
        private Date createdAt;
        private Date updatedAt;
}
