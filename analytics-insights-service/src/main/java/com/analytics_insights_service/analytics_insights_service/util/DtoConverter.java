package com.analytics_insights_service.analytics_insights_service.util;

import com.analytics_insights_service.analytics_insights_service.dto.SOPDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SOPDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SOPDto.class);
    }

    public static String sopDtoTojson(SOPDto sopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(sopDto);
    }

}
