package compliance_reporting_service.compliance_reporting_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import compliance_reporting_service.compliance_reporting_service.dto.ReportDto;
import compliance_reporting_service.compliance_reporting_service.dto.ReportResponseDto;
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

import java.util.ArrayList;
import java.util.List;


@Service
public class SOPReportService {

    private static final Logger log = LoggerFactory.getLogger(SOPReportService.class);


    private final SOPReportRepository repository;
    private final VersionClientService versionClientService;


    @Autowired
    public SOPReportService(SOPReportRepository repository, VersionClientService versionClientService) {
        this.repository = repository;
        this.versionClientService = versionClientService;
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

            // Convert the incoming data to a FeedbackDto using the ObjectMapper
            ReportDto reportDto = objectMapper.readValue(data, ReportDto.class);

            // Create a new FeedbackModel and set its fields from the DTO
            SOPReport sopReport = new SOPReport();
            sopReport.setId(new ObjectId().toString());
            sopReport.setSopId(reportDto.getId());
            sopReport.setTitle(reportDto.getTitle());
            sopReport.setVisibility(reportDto.getVisibility());
            sopReport.setCreatedAt(reportDto.getCreatedAt());
            sopReport.setUpdateAt(reportDto.getUpdatedAt());




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

        for(SOPReport report : sopReport) {
            reportResponseDtos.add(mapReportToReportResponseDTO(report));
        }

        return reportResponseDtos;
    }

public ReportResponseDto mapReportToReportResponseDTO(SOPReport sopReport) {
        GetSopVersionsResponse versionsResponse = versionClientService.GetSopVersions(sopReport.getSopId());

        if(!versionsResponse.getSuccess()){
          log.error("Error fetching versions for sop with id: {}", sopReport.getSopId());
        }
        return ReportResponseDto
                .builder()
                .title(sopReport.getTitle())
                .numberOfVersions(versionsResponse.getVersionsCount())
                .createdAt(sopReport.getCreatedAt())
                .updateAt(sopReport.getUpdateAt())
                .reads(sopReport.getReads())
                .visibility(sopReport.getVisibility())
                .build();
    }

}

