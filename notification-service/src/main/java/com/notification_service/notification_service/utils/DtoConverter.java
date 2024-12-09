package com.notification_service.notification_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.notification_service.notification_service.dtos.CustomUserDto;
import com.notification_service.notification_service.dtos.SOPDto;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CustomUserDto userDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, CustomUserDto.class);
    }

    public static String userDtoTojson(CustomUserDto customUserDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(customUserDto);
    }

    public static SOPDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SOPDto.class);
    }

    public static String sopDtoTojson(SOPDto sopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(sopDto);
    }
}