package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.StatusCountDto;
import com.analytics_insights_service.analytics_insights_service.dto.SOPStatusOverviewResponseDto;
import com.analytics_insights_service.analytics_insights_service.dto.SopByStatusDto;
import com.analytics_insights_service.analytics_insights_service.enums.SOPStatus;
import org.springframework.stereotype.Service;
import sopWorkflowService.GetDepartmentSopsByStatusResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final SopWorkflowClientService sopWorkflowClientService;

    public AnalyticsService(SopWorkflowClientService sopWorkflowClientService) {
        this.sopWorkflowClientService = sopWorkflowClientService;
    }

    public List<SopByStatusDto> getDepartmentSopsByStatus(UUID departmentId) {
        GetDepartmentSopsByStatusResponse response = sopWorkflowClientService.getDepartmentSopsByStatus(departmentId);
        return response.getSopsList().stream()
                .map(sop -> SopByStatusDto.builder()
                        .id(sop.getId())
                        .title(sop.getTitle())
                        .status(SOPStatus.valueOf(sop.getStatus()))
                        .createdAt(parseDate(sop.getCreatedAt()))
                        .updatedAt(parseDate(sop.getUpdatedAt()))
                        .build())
                .collect(Collectors.toList());

    }


    public List<SopByStatusDto> getSopsByStatus() {
        GetDepartmentSopsByStatusResponse response = sopWorkflowClientService.getSopsByStatusRequest();
        return response.getSopsList().stream()
                .map(sop -> SopByStatusDto.builder()
                        .id(sop.getId())
                        .title(sop.getTitle())
                        .status(SOPStatus.valueOf(sop.getStatus()))
                        .createdAt(parseDate(sop.getCreatedAt()))
                        .updatedAt(parseDate(sop.getUpdatedAt()))
                        .build())
                .collect(Collectors.toList());
    }

    public SOPStatusOverviewResponseDto getSopOverview(UUID departmentId, String role, String timeframe){
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        DateTimeFormatter formatter;
        int periods = 6;

        // Set up time period based on timeFrame parameter
        switch (timeframe.toLowerCase()) {
            case "daily":
                startDate = endDate.minusDays(5);
                formatter = DateTimeFormatter.ofPattern("dd MMM");
                break;
            case "monthly":
                startDate = endDate.minusMonths(5);
                formatter = DateTimeFormatter.ofPattern("MMM");
                break;
            default:
                throw new IllegalArgumentException("Invalid timeFrame. Use 'daily' or 'monthly'");
        }

        // Create a map to store counts for each month
        Map<String, StatusCountDto> monthlyCountsMap = new TreeMap<>();
        List<String> periodLabels = new ArrayList<>();

        // Initialize counts for all months in range
        for (int i = 0; i < periods; i++) {
            LocalDateTime currentPeriod = timeframe.equalsIgnoreCase("daily")
                    ? startDate.plusDays(i)
                    : startDate.plusMonths(i);

            String periodKey = currentPeriod.format(formatter);
            periodLabels.add(periodKey);

            StatusCountDto monthCount = new StatusCountDto();
            monthCount.setPeriod(periodKey);
            monthCount.setPublished(0);
            monthCount.setInitialized(0);
            monthCount.setUnderReview(0);
            monthCount.setDraft(0);

            monthlyCountsMap.put(periodKey, monthCount);
        }

        List<SopByStatusDto> sops = role.equals("ADMIN")
                ? getSopsByStatus()
                : getDepartmentSopsByStatus(departmentId);


        for(SopByStatusDto sop : sops){

            Date sopDate = sop.getCreatedAt();

            if (sopDate != null) {
                // Convert sopDate to LocalDateTime
                LocalDateTime sopDateTime = sopDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                // Only process SOPs within our date range
                if (sopDateTime.isAfter(startDate) && sopDateTime.isBefore(endDate.plusDays(1))) {
                    String monthKey = sopDateTime.format(DateTimeFormatter.ofPattern("MMM"));
                    StatusCountDto monthCount = monthlyCountsMap.get(monthKey);

                    if (monthCount != null) {
                        switch (sop.getStatus()) {
                            case SOPStatus.PUBLISHED:
                                monthCount.setPublished(monthCount.getPublished() + 1);
                                break;
                            case SOPStatus.UNDER_REVIEWAL:
                                monthCount.setUnderReview(monthCount.getUnderReview() + 1);
                                break;
                            case SOPStatus.INITIALIZED:
                                monthCount.setInitialized(monthCount.getInitialized() + 1);
                                break;
                            case SOPStatus.DRAFTED:
                                monthCount.setDraft(monthCount.getDraft() + 1);
                                break;
                        }
                    }

                }
            }
        }

        return SOPStatusOverviewResponseDto.builder()
                .counts(monthlyCountsMap)
                .periods(periodLabels)
                .build();
    }

    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: " + dateStr, e);
        }
    }
}
