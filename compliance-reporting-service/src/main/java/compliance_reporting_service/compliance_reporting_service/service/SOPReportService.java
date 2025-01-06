package compliance_reporting_service.compliance_reporting_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

@Service
public class SOPReportService {

    private static final Logger log = LoggerFactory.getLogger(SOPReportService.class);

    private final SOPReportRepository repository;
    private final VersionClientService versionClientService;
    private final UserInfoClientService userInfoClientService;

    @Autowired
    public SOPReportService(SOPReportRepository repository, VersionClientService versionClientService, UserInfoClientService userInfoClientService) {
        this.repository = repository;
        this.versionClientService = versionClientService;
        this.userInfoClientService = userInfoClientService;
    }

    @KafkaListener(
            topics = "sop-created",
            groupId = "compliance-reporting-service"
    )
    public void ReportCreatedListener(String data) throws JsonProcessingException {
        log.info("Received SOP created event: {}", data);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            SOPDto sopDto = objectMapper.readValue(data, SOPDto.class);

            SOPReport sopReport = new SOPReport();
            sopReport.setId(new ObjectId().toString());
            sopReport.setSopId(sopDto.getId());
            sopReport.setTitle(sopDto.getTitle());
            sopReport.setVisibility(sopDto.getVisibility());
            sopReport.setCreatedAt(sopDto.getCreatedAt());
            sopReport.setUpdatedAt(sopDto.getUpdatedAt());
            sopReport.setAuthorId(sopDto.getAuthorId());
            sopReport.setReviewers(sopDto.getReviewers());
            sopReport.setApproverId(sopDto.getApproverId());

            repository.save(sopReport);
            log.info("Saved SOPReport model: {}", sopReport);
        } catch (Exception e) {
            log.error("Error processing SOP created event: {}", e.getMessage(), e);
        }
    }

    public List<ReportResponseDto> findAll() {
        List<SOPReport> sopReports = repository.findAll();
        List<ReportResponseDto> reportResponseDtos = new ArrayList<>();

        for (SOPReport report : sopReports) {
            reportResponseDtos.add(mapReportToReportResponseDTO(report));
        }

        return reportResponseDtos;
    }

    public SOPReport findReportById(String reportId) {
        return repository.findById(reportId).orElse(null);
    }

    public ReportResponseDto mapReportToReportResponseDTO(SOPReport sopReport) {
        GetSopVersionsResponse versionsResponse = versionClientService.GetSopVersions(sopReport.getSopId());
        if (!versionsResponse.getSuccess()) {
            log.error("Error fetching versions for SOP with id: {}", sopReport.getSopId());
        }

        String authorName = "Unknown";
        if (sopReport.getAuthorId() != null) {
            getUserInfoResponse authorInfo = userInfoClientService.getUserInfo(sopReport.getAuthorId().toString());
            if (authorInfo.getSuccess()) {
                authorName = authorInfo.getName();
            } else {
                log.error("Error fetching author info: {}", authorInfo.getErrorMessage());
            }
        }

        String approverName = "Unknown";
        if (sopReport.getApproverId() != null) {
            getUserInfoResponse approverInfo = userInfoClientService.getUserInfo(sopReport.getApproverId().toString());
            if (approverInfo.getSuccess()) {
                approverName = approverInfo.getName();
            } else {
                log.error("Error fetching approver info: {}", approverInfo.getErrorMessage());
            }
        }

        List<String> reviewersInfo = new ArrayList<>();
        if (sopReport.getReviewers() != null) {
            for (UUID reviewerId : sopReport.getReviewers()) {
                if (reviewerId != null) {
                    getUserInfoResponse reviewerInfo = userInfoClientService.getUserInfo(reviewerId.toString());
                    if (reviewerInfo.getSuccess()) {
                        reviewersInfo.add(reviewerInfo.getName());
                    } else {
                        log.error("Error fetching reviewer info: {}", reviewerInfo.getErrorMessage());
                    }
                }
            }
        }

        return ReportResponseDto
                .builder()
                .reportId(sopReport.getId())
                .title(sopReport.getTitle())
                .numberOfVersions(versionsResponse.getVersionsCount())
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