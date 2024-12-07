package com.sop_workflow_service.sop_workflow_service.dto;

import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import lombok.*;

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
        private List<StageDto> reviewers;
        private StageDto approver;
        private StageDto author;
}
