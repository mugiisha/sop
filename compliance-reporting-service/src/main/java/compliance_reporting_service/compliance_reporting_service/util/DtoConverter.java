package compliance_reporting_service.compliance_reporting_service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import compliance_reporting_service.compliance_reporting_service.dto.ReportDto;


public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert JSON to FeedbackDto
    public static ReportDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ReportDto.class);
    }

}