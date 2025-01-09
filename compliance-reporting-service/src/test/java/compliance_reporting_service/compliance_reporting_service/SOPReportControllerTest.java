package compliance_reporting_service.compliance_reporting_service;

import compliance_reporting_service.compliance_reporting_service.controller.SOPReportController;
import compliance_reporting_service.compliance_reporting_service.dto.ReportResponseDto;
import compliance_reporting_service.compliance_reporting_service.service.SOPReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class SOPReportControllerTest {

    @Mock
    private SOPReportService sopReportService;

    @InjectMocks
    private SOPReportController sopReportController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(sopReportController).build();
    }

    @Test
    void testGetAllReports() throws Exception {
        // Using the builder pattern to create the DTO
        ReportResponseDto reportResponseDto = ReportResponseDto.builder()
                .reportId("report-123")
                .title("Test SOP")
                .numberOfVersions(5)
                .visibility("Public")
                .reads(100)
                .createdAt(new Date())
                .updatedAt(new Date())
                .author("author-123")
                .approver("approver-456")
                .reviewers(List.of("reviewer-1", "reviewer-2"))
                .build();

        List<ReportResponseDto> reportResponseDtos = List.of(reportResponseDto);
        when(sopReportService.findAll()).thenReturn(reportResponseDtos);

        // Performing the GET request to the controller and asserting the response
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Report fetched successfully"))
                .andExpect(jsonPath("$.data[0].reportId").value("report-123"))
                .andExpect(jsonPath("$.data[0].title").value("Test SOP"))
                .andExpect(jsonPath("$.data[0].author").value("author-123"))
                .andExpect(jsonPath("$.data[0].approver").value("approver-456"))
                .andExpect(jsonPath("$.data[0].reviewers[0]").value("reviewer-1"))
                .andExpect(jsonPath("$.data[0].reviewers[1]").value("reviewer-2"));

        verify(sopReportService, times(1)).findAll();
    }
}
