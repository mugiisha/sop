package com.audit_compliance_tracking_service.audit_compliance_tracking_service.util;

import com.audit_compliance_tracking_service.audit_compliance_tracking_service.dto.AcknowledgedDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert JSON to AcknowledgedDto
    public static AcknowledgedDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, AcknowledgedDto.class);
    }
}
