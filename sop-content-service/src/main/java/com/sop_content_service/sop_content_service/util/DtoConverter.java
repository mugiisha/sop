package com.sop_content_service.sop_content_service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.sop_content_service.sop_content_service.dto.SOPDto;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SOPDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SOPDto.class);
    }

    public static String sopDtoTojson(SOPDto sopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(sopDto);
    }

}
