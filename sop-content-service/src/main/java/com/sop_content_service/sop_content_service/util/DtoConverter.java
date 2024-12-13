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
        // Safely parse authors and approvers
        UUID authorUuid = parseUuid(String.valueOf(sopModel.getAuthors()));
        UUID approverUuid = parseUuid(String.valueOf(sopModel.getApprovers()));

        return new SOPDto(
                sopModel.getId(),
                sopModel.getTitle(),
                sopModel.getVisibility(),
                authorUuid, // First author safely converted
                sopModel.getCategoryId(),
                sopModel.getReviewers().stream()
                        .map(DtoConverter::parseUuid) // Convert reviewers safely
                        .collect(Collectors.toList()),
                approverUuid // First approver safely converted
        );
    }

    // Utility method to parse UUIDs safely
    private static UUID parseUuid(String uuidString) {
        try {
            return uuidString != null ? UUID.fromString(uuidString) : null;
        } catch (IllegalArgumentException e) {
            return null; // Handle invalid UUID gracefully
        }
    }
}
