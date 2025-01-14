package com.analytics_insights_service.analytics_insights_service.dto;

import com.analytics_insights_service.analytics_insights_service.enums.SOPStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SopByStatusDto {
    private String id;
    private String title;
    private SOPStatus status;
    private Date createdAt;
    private Date updatedAt;
}
