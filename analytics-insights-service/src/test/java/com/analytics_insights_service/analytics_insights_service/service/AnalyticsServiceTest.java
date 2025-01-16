package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.SOPStatusOverviewResponseDto;
import com.analytics_insights_service.analytics_insights_service.dto.SopByStatusDto;
import com.analytics_insights_service.analytics_insights_service.enums.SOPStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopWorkflowService.GetDepartmentSopsByStatusResponse;
import sopWorkflowService.SopByStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SopWorkflowClientService sopWorkflowClientService;

    private AnalyticsService analyticsService;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(sopWorkflowClientService);
        departmentId = UUID.randomUUID();
    }

    @Test
    void getDepartmentSopsByStatus_ShouldReturnMappedSops() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.atZone(ZoneId.systemDefault()).toString();

        List<SopByStatus> mockSops = Arrays.asList(
                createMockSop(UUID.randomUUID(), "SOP 1", "PUBLISHED", dateStr),
                createMockSop(UUID.randomUUID(), "SOP 2", "DRAFTED", dateStr)
        );

        GetDepartmentSopsByStatusResponse mockResponse = GetDepartmentSopsByStatusResponse.newBuilder()
                .addAllSops(mockSops)
                .build();

        when(sopWorkflowClientService.getDepartmentSopsByStatus(departmentId))
                .thenReturn(mockResponse);

        // Act
        List<SopByStatusDto> result = analyticsService.getDepartmentSopsByStatus(departmentId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("SOP 1", result.get(0).getTitle());
        assertEquals(SOPStatus.PUBLISHED, result.get(0).getStatus());
        assertEquals("SOP 2", result.get(1).getTitle());
        assertEquals(SOPStatus.DRAFTED, result.get(1).getStatus());

        verify(sopWorkflowClientService).getDepartmentSopsByStatus(departmentId);
    }

    @Test
    void getSopsByStatus_ShouldReturnAllSops() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.atZone(ZoneId.systemDefault()).toString();

        List<SopByStatus> mockSops = Arrays.asList(
                createMockSop(UUID.randomUUID(), "SOP 1", "PUBLISHED", dateStr),
                createMockSop(UUID.randomUUID(), "SOP 2", "UNDER_REVIEWAL", dateStr)
        );

        GetDepartmentSopsByStatusResponse mockResponse = GetDepartmentSopsByStatusResponse.newBuilder()
                .addAllSops(mockSops)
                .build();

        when(sopWorkflowClientService.getSopsByStatusRequest())
                .thenReturn(mockResponse);

        // Act
        List<SopByStatusDto> result = analyticsService.getSopsByStatus();

        // Assert
        assertEquals(2, result.size());
        assertEquals("SOP 1", result.get(0).getTitle());
        assertEquals(SOPStatus.PUBLISHED, result.get(0).getStatus());
        assertEquals("SOP 2", result.get(1).getTitle());
        assertEquals(SOPStatus.UNDER_REVIEWAL, result.get(1).getStatus());

        verify(sopWorkflowClientService).getSopsByStatusRequest();
    }

    @Test
    void getSopOverview_Daily_ShouldReturnCorrectPeriods() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.atZone(ZoneId.systemDefault()).toString();

        List<SopByStatus> mockSops = Arrays.asList(
                createMockSop(UUID.randomUUID(), "SOP 1", "PUBLISHED", dateStr),
                createMockSop(UUID.randomUUID(), "SOP 2", "DRAFTED", dateStr)
        );

        GetDepartmentSopsByStatusResponse mockResponse = GetDepartmentSopsByStatusResponse.newBuilder()
                .addAllSops(mockSops)
                .build();

        when(sopWorkflowClientService.getDepartmentSopsByStatus(departmentId))
                .thenReturn(mockResponse);

        // Act
        SOPStatusOverviewResponseDto result = analyticsService.getSopOverview(departmentId, "USER", "daily");

        // Assert
        assertNotNull(result);
        assertEquals(6, result.getPeriods().size());
        assertNotNull(result.getCounts());

        verify(sopWorkflowClientService).getDepartmentSopsByStatus(departmentId);
    }

    @Test
    void getSopOverview_Monthly_ShouldReturnCorrectPeriods() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.atZone(ZoneId.systemDefault()).toString();

        List<SopByStatus> mockSops = Arrays.asList(
                createMockSop(UUID.randomUUID(), "SOP 1", "PUBLISHED", dateStr),
                createMockSop(UUID.randomUUID(), "SOP 2", "UNDER_REVIEWAL", dateStr)
        );

        GetDepartmentSopsByStatusResponse mockResponse = GetDepartmentSopsByStatusResponse.newBuilder()
                .addAllSops(mockSops)
                .build();

        when(sopWorkflowClientService.getSopsByStatusRequest())
                .thenReturn(mockResponse);

        // Act
        SOPStatusOverviewResponseDto result = analyticsService.getSopOverview(departmentId, "ADMIN", "monthly");

        // Assert
        assertNotNull(result);
        assertEquals(6, result.getPeriods().size());
        assertNotNull(result.getCounts());

        verify(sopWorkflowClientService).getSopsByStatusRequest();
    }

    @Test
    void getSopOverview_InvalidTimeframe_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                analyticsService.getSopOverview(departmentId, "USER", "invalid"));
    }

    private SopByStatus createMockSop(UUID id, String title, String status, String date) {
        return SopByStatus.newBuilder()
                .setId(id.toString())
                .setTitle(title)
                .setStatus(status)
                .setCreatedAt(date)
                .setUpdatedAt(date)
                .build();
    }
}