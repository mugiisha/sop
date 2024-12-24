package com.sop_workflow_service.sop_workflow_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_workflow_service.sop_workflow_service.dto.PublishedSopDto;
import com.sop_workflow_service.sop_workflow_service.dto.SOPDto;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static SOPDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SOPDto.class);
    }

    public static String sopDtoTojson(SOPDto sopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(sopDto);
    }

    public static PublishedSopDto publishedSopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, PublishedSopDto.class);
    }

    public static String publishedSopDtoTojson(PublishedSopDto publishedSopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(publishedSopDto);
    }
}