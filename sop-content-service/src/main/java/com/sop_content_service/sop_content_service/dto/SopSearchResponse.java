package com.sop_content_service.sop_content_service.dto;

import lombok.Data;

@Data
public class SopSearchResponse<T> {
    private T data;
    private String message;
    private String error;
    private SearchMetadata metadata;

    public SopSearchResponse(T data, String message, SearchMetadata metadata) {
        this.data = data;
        this.message = message;
        this.metadata = metadata;
    }

    public SopSearchResponse(String message, String error) {
        this.message = message;
        this.error = error;
    }
}