package com.sop_content_service.sop_content_service.dto;

import com.sop_content_service.sop_content_service.enums.SOPStatus;
import com.sop_content_service.sop_content_service.enums.Visibility;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SOPResponseDto {
        private String id;
        private List<String> documentUrls;
        private String coverUrl;
        private String title;
        private String description;
        private String body;
        private String category;
        private UUID departmentId;
        private Visibility visibility;
        private SOPStatus status;
        private List<SopVersionDto> versions;
        private List<StageDto> reviewers;
        private StageDto approver;
        private StageDto author;
        private Date createdAt;
        private Date updatedAt;
}
