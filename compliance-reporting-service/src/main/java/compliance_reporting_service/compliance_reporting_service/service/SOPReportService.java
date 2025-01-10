package compliance_reporting_service.compliance_reporting_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import compliance_reporting_service.compliance_reporting_service.dto.ReportDto;
import compliance_reporting_service.compliance_reporting_service.dto.ReportResponseDto;
import compliance_reporting_service.compliance_reporting_service.dto.SOPDto;
import compliance_reporting_service.compliance_reporting_service.model.SOPReport;
import compliance_reporting_service.compliance_reporting_service.repository.SOPReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import sopVersionService.GetSopVersionsResponse;
import userService.getUserInfoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

@Service
public class SOPReportService {

    private static final Logger log = LoggerFactory.getLogger(SOPReportService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final SOPReportRepository repository;
    private final VersionClientService versionClientService;
    private final UserInfoClientService userInfoClientService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SOPReportService(SOPReportRepository repository,
                            VersionClientService versionClientService,
                            UserInfoClientService userInfoClientService) {
        this.repository = repository;
        this.versionClientService = versionClientService;
        this.userInfoClientService = userInfoClientService;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(
            topics = "sop-created",
            groupId = "compliance-reporting-service"
    )
    public void ReportCreatedListener(String data) {
        log.info("Received SOP created event: {}", data);
        try {
            SOPDto sopDto = objectMapper.readValue(data, SOPDto.class);

            // Validate required fields
            if (!isValidSOPDto(sopDto)) {
                log.error("Invalid SOP DTO received: {}", data);
                return;
            }

            SOPReport sopReport = createSOPReportFromDto(sopDto);
            repository.save(sopReport);
            log.info("Successfully saved SOP report: {}", sopReport.getId());

        } catch (JsonProcessingException e) {
            log.error("Error parsing SOP created event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing SOP created event: {}", e.getMessage(), e);
        }
    }

    private boolean isValidSOPDto(SOPDto sopDto) {
        if (sopDto == null) return false;
        if (sopDto.getId() == null) {
            log.error("SOP DTO missing required field: id");
            return false;
        }
        if (sopDto.getTitle() == null || sopDto.getTitle().trim().isEmpty()) {
            log.warn("SOP DTO missing title, will use default");
        }
        return true;
    }

    private SOPReport createSOPReportFromDto(SOPDto sopDto) {
        SOPReport sopReport = new SOPReport();
        sopReport.setId(new ObjectId().toString());
        sopReport.setSopId(sopDto.getId());
        sopReport.setTitle(sopDto.getTitle() != null ? sopDto.getTitle().trim() : "Untitled SOP");
        sopReport.setVisibility(sopDto.getVisibility());
        sopReport.setCreatedAt(sopDto.getCreatedAt());
        sopReport.setUpdatedAt(sopDto.getUpdatedAt());
        sopReport.setAuthorId(sopDto.getAuthorId());
        sopReport.setReviewers(sopDto.getReviewers() != null ? sopDto.getReviewers() : new ArrayList<>());
        sopReport.setApproverId(sopDto.getApproverId());
        return sopReport;
    }

    public List<ReportResponseDto> findAll() {
        List<SOPReport> sopReports = repository.findAll();
        List<ReportResponseDto> reportResponseDtos = new ArrayList<>();

        for (SOPReport report : sopReports) {
            try {
                reportResponseDtos.add(mapReportToReportResponseDTO(report));
            } catch (Exception e) {
                log.error("Error mapping report {}: {}", report.getId(), e.getMessage());
            }
        }

        return reportResponseDtos;
    }

    public SOPReport findReportById(String reportId) {
        return repository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public ReportResponseDto mapReportToReportResponseDTO(SOPReport sopReport) {
        // Default values
        String authorName = "Unknown";
        String approverName = "Unknown";
        List<String> reviewersInfo = new ArrayList<>();
        int versionsCount = 0;

        // Get versions info
        if (sopReport.getSopId() != null) {
            GetSopVersionsResponse versionsResponse = versionClientService.GetSopVersions(sopReport.getSopId());
            if (versionsResponse != null && versionsResponse.getSuccess()) {
                versionsCount = versionsResponse.getVersionsCount();
            } else {
                log.warn("Could not fetch versions for SOP: {}", sopReport.getSopId());
            }
        }

        // Get author info
        if (sopReport.getAuthorId() != null) {
            getUserInfoResponse authorInfo = userInfoClientService.getUserInfo(sopReport.getAuthorId().toString());
            if (authorInfo != null && authorInfo.getSuccess()) {
                authorName = authorInfo.getName();
            }
        }

        // Get approver info
        if (sopReport.getApproverId() != null) {
            getUserInfoResponse approverInfo = userInfoClientService.getUserInfo(sopReport.getApproverId().toString());
            if (approverInfo != null && approverInfo.getSuccess()) {
                approverName = approverInfo.getName();
            }
        }

        // Get reviewers info
        if (sopReport.getReviewers() != null) {
            for (UUID reviewerId : sopReport.getReviewers()) {
                if (reviewerId != null) {
                    try {
                        getUserInfoResponse reviewerInfo = userInfoClientService.getUserInfo(reviewerId.toString());
                        String reviewerName = (reviewerInfo != null && reviewerInfo.getSuccess())
                                ? reviewerInfo.getName()
                                : "Unknown Reviewer";
                        reviewersInfo.add(reviewerName);
                    } catch (Exception e) {
                        log.error("Error fetching reviewer info for {}: {}", reviewerId, e.getMessage());
                        reviewersInfo.add("Unknown Reviewer");
                    }
                }
            }
        }

        // Build and return the DTO
        return ReportResponseDto.builder()
                .reportId(sopReport.getId())
                .title(sopReport.getTitle())
                .numberOfVersions(versionsCount)
                .createdAt(sopReport.getCreatedAt())
                .updatedAt(sopReport.getUpdatedAt())
                .reads(sopReport.getReads())
                .visibility(sopReport.getVisibility())
                .author(authorName)
                .approver(approverName)
                .reviewers(reviewersInfo)
                .build();
    }
}