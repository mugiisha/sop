package com.sop_workflow_service.sop_workflow_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_workflow_service.sop_workflow_service.dto.SOPDto;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static SOPDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SOPDto.class);
    }

    public static String sopDtoTojson(SOPDto sopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(sopDto);
    }
}