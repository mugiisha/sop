package com.sop_content_service.sop_content_service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.sop_content_service.sop_content_service.dto.SOPDto;
import com.sop_content_service.sop_content_service.model.SopModel;

import java.util.UUID;
import java.util.stream.Collectors;

public class DtoConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SOPDto sopDtoFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SOPDto.class);
    }

    public static String sopDtoTojson(SOPDto sopDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(sopDto);
    }


    // Converts SopModel (entity) to SOPDto
    public static SOPDto sopDtoFromEntity(SopModel sopModel) {
        return new SOPDto(
                sopModel.getId(),
                sopModel.getTitle(),
                sopModel.getVisibility(),
                UUID.fromString(String.valueOf(sopModel.getAuthors())), // Assuming the first author as main author
                sopModel.getCategoryId(),
                sopModel.getReviewers().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList()),
                UUID.fromString(String.valueOf(sopModel.getApprovers())) // Assuming the first approver as main approver
        );
    }
}
