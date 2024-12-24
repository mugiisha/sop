package compliance_reporting_service.compliance_reporting_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import compliance_reporting_service.compliance_reporting_service.dto.ReportDto;
import compliance_reporting_service.compliance_reporting_service.model.SOPReport;
import compliance_reporting_service.compliance_reporting_service.repository.SOPReportRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class SOPReportService {

    private static final Logger log = LoggerFactory.getLogger(SOPReportService.class);


    private final SOPReportRepository repository;


    @Autowired
    public SOPReportService(SOPReportRepository repository) {
        this.repository = repository;

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
            sopReport.setCreatedAt(reportDto.getCreatedAt().toLocalDate());
            sopReport.setUpdateAt(reportDto.getUpdatedAt().toLocalDate());




            // Save the model to the database
            repository.save(sopReport);
            log.info("Saved Feedback model: {}", sopReport);
        } catch (Exception e) {
            log.error("Error processing SOP created event: {}", e.getMessage(), e);
        }
    }


}

