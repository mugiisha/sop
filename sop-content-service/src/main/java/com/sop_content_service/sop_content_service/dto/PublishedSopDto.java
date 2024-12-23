package com.sop_content_service.sop_content_service.dto;


import com.sop_content_service.sop_content_service.enums.SOPStatus;
import com.sop_content_service.sop_content_service.enums.Visibility;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PublishedSopDto {
    private String id;
    private List<String> documentUrls;
    private String coverUrl;
    private String title;
    private String description;
    private String body;
    private String category;
    private UUID departmentId;
    private Visibility visibility;
    private UUID author;
    private List<UUID> reviewers;
    private UUID approver;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
}
