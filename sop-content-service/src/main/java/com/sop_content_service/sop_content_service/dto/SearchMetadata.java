package com.sop_content_service.sop_content_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SearchMetadata {
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;  // Added this field as it's used in createSearchMetadata
    private String sortBy;
    private String sortOrder;
    private List<String> appliedFilters;
}