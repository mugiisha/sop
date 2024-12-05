package com.sop_content_service.sop_content_service.dto;

import lombok.Data;

@Data
public class SopSearchRequest {
    private String keyword;
    private String department;
    private String category;
    private String status;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
}