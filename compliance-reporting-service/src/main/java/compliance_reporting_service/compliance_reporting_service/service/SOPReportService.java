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
    /**
     * Consumer method for the Kafka topic sop-created
     */
    public void ReportCreatedListener(String data) throws JsonProcessingException {
        log.info("Received SOP created event: {}", data);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Convert the incoming data to a ReportDto using the ObjectMapper
            SOPDto sopDto = objectMapper.readValue(data, SOPDto.class);

            // Create a new ReportModel and set its fields from the DTO
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




            // Save the model to the database
            repository.save(sopReport);
            log.info("Saved Feedback model: {}", sopReport);
        } catch (Exception e) {
            log.error("Error processing SOP created event: {}", e.getMessage(), e);
        }
    }

    // Read All
    public List<ReportResponseDto> findAll() {
        List<SOPReport> sopReport = repository.findAll();
        List<ReportResponseDto> reportResponseDtos = new ArrayList<>();

        for (SOPReport report : sopReport) {
            reportResponseDtos.add(mapReportToReportResponseDTO(report));
        }

        return reportResponseDtos;
    }
    // Fetch a report by its ID
    public SOPReport findReportById(String reportId) {
        return repository.findById(reportId).orElse(null); // Fetch the report by ID using repository
    }

    // Mapping SOPReport to ReportResponseDto

    public ReportResponseDto mapReportToReportResponseDTO(SOPReport sopReport) {
        // Fetch versions response
        GetSopVersionsResponse versionsResponse = versionClientService.GetSopVersions(sopReport.getSopId());
        if (!versionsResponse.getSuccess()) {
            log.error("Error fetching versions for sop with id: {}", sopReport.getSopId());
        }

        // Fetch user information (assuming you have access to userId from sopReport or elsewhere)

        getUserInfoResponse authorInfo = userInfoClientService.getUserInfo(sopReport.getAuthorId().toString());
        if (!authorInfo.getSuccess()) {
            log.error("Error fetching user info: {}", authorInfo.getErrorMessage());
        }

        getUserInfoResponse approverInfo = userInfoClientService.getUserInfo(sopReport.getApproverId().toString());
        if (!approverInfo.getSuccess()) {
            log.error("Error fetching user info: {}", approverInfo.getErrorMessage());
        }

        List<String> reviewersInfo = new ArrayList<>();

        for(UUID reviewerId : sopReport.getReviewers()){
            getUserInfoResponse reviewerInfo = userInfoClientService.getUserInfo(reviewerId.toString());
            if (!reviewerInfo.getSuccess()) {
                log.error("Error fetching user info: {}", reviewerInfo.getErrorMessage());
            }
            reviewersInfo.add(reviewerInfo.getName());
        }


        // Map to DTO
        return ReportResponseDto
                .builder()
                .reportId(sopReport.getId())
                .title(sopReport.getTitle())
                .numberOfVersions(versionsResponse.getVersionsCount())
                .createdAt(sopReport.getCreatedAt())
                .updatedAt(sopReport.getUpdatedAt())
                .reads(sopReport.getReads())
                .visibility(sopReport.getVisibility())
                .author(authorInfo.getName()) // Add userId to the response
                .approver(approverInfo.getName())
                .reviewers(reviewersInfo)
                .build();
    }

}
