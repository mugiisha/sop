package com.analytics_insights_service.analytics_insights_service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.analytics_insights_service.analytics_insights_service.dto.FeedbackDto;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;

import java.util.UUID;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert JSON to FeedbackDto
    public static FeedbackDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, FeedbackDto.class);
    }

}