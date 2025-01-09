//package compliance_reporting_service.compliance_reporting_service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import compliance_reporting_service.compliance_reporting_service.dto.ReportResponseDto;
//import compliance_reporting_service.compliance_reporting_service.dto.SOPDto;
//import compliance_reporting_service.compliance_reporting_service.model.SOPReport;
//import compliance_reporting_service.compliance_reporting_service.repository.SOPReportRepository;
//import compliance_reporting_service.compliance_reporting_service.service.SOPReportService;
//import compliance_reporting_service.compliance_reporting_service.service.UserInfoClientService;
//import compliance_reporting_service.compliance_reporting_service.service.VersionClientService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import sopVersionService.GetSopVersionsResponse;
//import sopVersionService.SopVersion;
//import userService.getUserInfoResponse;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class SOPReportServiceTest {
//
//    @Mock
//    private SOPReportRepository repository;
//
//    @Mock
//    private VersionClientService versionClientService;
//
//    @Mock
//    private UserInfoClientService userInfoClientService;
//
//    @InjectMocks
//    private SOPReportService sopReportService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testReportCreatedListener() throws JsonProcessingException {
//        String kafkaData = "{ \"id\": \"123\", \"title\": \"Test SOP\", \"visibility\": \"PUBLIC\", " +
//                "\"createdAt\": \"2024-01-01T00:00:00Z\", \"updatedAt\": \"2024-01-02T00:00:00Z\", " +
//                "\"authorId\": \"550e8400-e29b-41d4-a716-446655440000\", " +
//                "\"reviewers\": [\"550e8400-e29b-41d4-a716-446655440001\"], " +
//                "\"approverId\": \"550e8400-e29b-41d4-a716-446655440002\" }";
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        SOPDto sopDto = objectMapper.readValue(kafkaData, SOPDto.class);
//
//        sopReportService.reportCreatedListener(kafkaData);
//
//        ArgumentCaptor<SOPReport> reportCaptor = ArgumentCaptor.forClass(SOPReport.class);
//        verify(repository, times(1)).save(reportCaptor.capture());
//
//        SOPReport savedReport = reportCaptor.getValue();
//        assertNotNull(savedReport);
//        assertEquals(sopDto.getId(), savedReport.getSopId());
//        assertEquals(sopDto.getTitle(), savedReport.getTitle());
//        assertEquals(sopDto.getVisibility(), savedReport.getVisibility());
//        assertEquals(sopDto.getAuthorId(), savedReport.getAuthorId());
//        assertEquals(sopDto.getReviewers(), savedReport.getReviewers());
//        assertEquals(sopDto.getApproverId(), savedReport.getApproverId());
//    }
//
//    @Test
//    void testFindAll() {
//        SOPReport report = new SOPReport();
//        report.setSopId("sop-123");
//        when(repository.findAll()).thenReturn(List.of(report));
//
//        GetSopVersionsResponse mockResponse = GetSopVersionsResponse.newBuilder()
//                .setSuccess(true)
//                .addVersions(SopVersion.newBuilder().setVersionNumber(1.0f).setCurrentVersion(true).build())
//                .build();
//
//        when(versionClientService.GetSopVersions("sop-123")).thenReturn(mockResponse);
//
//        List<ReportResponseDto> response = sopReportService.findAll();
//
//        assertEquals(1, response.size());
//        assertEquals("sop-123", response.get(0).getReportId());
//        assertNotNull(response.get(0).getCreatedAt());
//        assertNotNull(response.get(0).getUpdatedAt());
//        assertEquals(1, response.get(0).getNumberOfVersions());
//    }
//
//    @Test
//    void testFindReportById() {
//        String reportId = "report-123";
//        SOPReport mockReport = new SOPReport();
//        mockReport.setId(reportId);
//        when(repository.findById(reportId)).thenReturn(java.util.Optional.of(mockReport));
//
//        SOPReport report = sopReportService.findReportById(reportId);
//
//        assertNotNull(report);
//        assertEquals(reportId, report.getId());
//    }
//}
