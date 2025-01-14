package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.dto.SOPStatusOverviewResponseDto;
import com.analytics_insights_service.analytics_insights_service.dto.SopByStatusDto;
import com.analytics_insights_service.analytics_insights_service.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public SOPStatusOverviewResponseDto getAnalytics(@RequestParam String timeframe, HttpServletRequest request) {
        UUID departmentId = UUID.fromString(request.getHeader("X-Department-Id"));
        String role = request.getHeader("X-User-Role");
        return analyticsService.getSopOverview(departmentId, role, timeframe);
    }
}
