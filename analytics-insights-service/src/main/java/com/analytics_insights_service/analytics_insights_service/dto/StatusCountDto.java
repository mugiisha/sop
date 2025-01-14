package com.analytics_insights_service.analytics_insights_service.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatusCountDto {
    private String period;
    private int published;
    private int underReview;
    private int draft;
    private int initialized;
}
