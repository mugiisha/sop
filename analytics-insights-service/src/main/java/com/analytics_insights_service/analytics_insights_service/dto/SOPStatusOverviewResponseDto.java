package com.analytics_insights_service.analytics_insights_service.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SOPStatusOverviewResponseDto {
    private Map<String, StatusCountDto> data;
    private List<String> periods;
}
