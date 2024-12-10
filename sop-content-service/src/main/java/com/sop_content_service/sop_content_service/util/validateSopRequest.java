package com.sop_content_service.sop_content_service.util;

import com.sop_content_service.sop_content_service.dto.SopRequest;
import jakarta.validation.Valid;

public class validateSopRequest {

    public static void validate(@Valid SopRequest sopRequest) {
        if (sopRequest.getDescription() == null || sopRequest.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (sopRequest.getBody() == null || sopRequest.getBody().isEmpty()) {
            throw new IllegalArgumentException("Body is required");
        }
    }
}
