//package com.sop_content_service.sop_content_service.service;
//
//import com.sop_content_service.sop_content_service.dto.*;
//import com.sop_content_service.sop_content_service.exception.*;
//import com.sop_content_service.sop_content_service.model.SopModel;
//import com.sop_content_service.sop_content_service.strategy.SearchContext;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//@Slf4j
//@Service
//public class SopSearchService {
//    private final SearchContext searchContext;
//    private static final int MAX_PAGE_SIZE = 100;
//    private static final String DEFAULT_SORT_FIELD = "createdAt";
//    private static final String DEFAULT_SORT_DIRECTION = "desc";
//
//    public SopSearchService(SearchContext searchContext) {
//        this.searchContext = searchContext;
//    }
//
//    @Cacheable(
//            value = "sopSearch",
//            key = "#searchRequest.hashCode() + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDir",
//            unless = "#result.body == null || #result.body.data.content.isEmpty()"
//    )
//    public ResponseEntity<SopSearchResponse<Page<SopModel>>> searchSOPs(
//            SopSearchRequest searchRequest,
//            int page,
//            int size,
//            String sortBy,
//            String sortDir) throws Exception {
//
//        log.debug("Executing search with parameters: request={}, page={}, size={}, sortBy={}, sortDir={}",
//                searchRequest, page, size, sortBy, sortDir);
//
//        try {
//            // Validate inputs
//            validateSearchRequest(searchRequest);
//            validatePaginationParams(page, size);
//
//            // Prepare pagination and sorting
//            sortBy = StringUtils.hasText(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
//            sortDir = StringUtils.hasText(sortDir) ? sortDir : DEFAULT_SORT_DIRECTION;
//            Sort sort = createSort(sortBy, sortDir);
//            Pageable pageable = PageRequest.of(page, size, sort);
//
//            // Execute search
//            Page<SopModel> results = searchContext.executeSearch(searchRequest, pageable);
//
//            // Handle empty results
//            if (results.isEmpty()) {
//                log.info("No SOPs found for search criteria: {}", searchRequest);
//                throw new SopNotFoundException("No SOPs found matching the search criteria");
//            }
//
//            // Create metadata and response
//            SearchMetadata metadata = createSearchMetadata(results, sortBy, sortDir, searchRequest);
//
//            SopSearchResponse<Page<SopModel>> response = new SopSearchResponse<>(
//                    results,
//                    "SOPs retrieved successfully",
//                    metadata
//            );
//
//            log.debug("Search completed successfully. Found {} results", results.getTotalElements());
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Error during SOP search: {}", e.getMessage(), e);
//            handleSearchException(e);
//            return ResponseEntity.badRequest().body(
//                    new SopSearchResponse<>("Error searching SOPs", e.getMessage())
//            );
//        }
//    }
//
//    private void validateSearchRequest(SopSearchRequest request) {
//        if (request == null) {
//            throw new InvalidSearchParameterException("Search request cannot be null");
//        }
//
//        if (!hasValidSearchCriteria(request)) {
//            throw new InvalidSearchParameterException("At least one search criterion must be provided");
//        }
//
//        if (StringUtils.hasText(request.getSortBy()) && !isValidSortField(request.getSortBy())) {
//            throw new InvalidSearchParameterException("Invalid sort field: " + request.getSortBy());
//        }
//    }
//
//    private boolean hasValidSearchCriteria(SopSearchRequest request) {
//        return StringUtils.hasText(request.getKeyword()) ||
//                StringUtils.hasText(request.getDepartment()) ||
//                StringUtils.hasText(request.getCategory()) ||
//                StringUtils.hasText(request.getStatus());
//    }
//
//    private boolean isValidSortField(String sortField) {
//        return Objects.equals(sortField, "title") ||
//                Objects.equals(sortField, "createdAt") ||
//                Objects.equals(sortField, "updatedAt") ||
//                Objects.equals(sortField, "department") ||
//                Objects.equals(sortField, "category") ||
//                Objects.equals(sortField, "status");
//    }
//
//    private void validatePaginationParams(int page, int size) {
//        if (page < 0) {
//            throw new InvalidSearchParameterException("Page number cannot be negative");
//        }
//        if (size <= 0 || size > MAX_PAGE_SIZE) {
//            throw new InvalidSearchParameterException(
//                    String.format("Page size must be between 1 and %d", MAX_PAGE_SIZE)
//            );
//        }
//    }
//
//    private Sort createSort(String sortBy, String sortDir) {
//        try {
//            Sort.Direction direction = Sort.Direction.fromString(sortDir);
//            return Sort.by(direction, sortBy);
//        } catch (IllegalArgumentException e) {
//            log.warn("Invalid sort parameters provided: sortBy={}, sortDir={}", sortBy, sortDir);
//            throw new InvalidSearchParameterException(
//                    "Invalid sort parameters. Direction must be 'asc' or 'desc'"
//            );
//        }
//    }
//
//    private SearchMetadata createSearchMetadata(
//            Page<SopModel> results,
//            String sortBy,
//            String sortDir,
//            SopSearchRequest request) {
//        return SearchMetadata.builder()
//                .totalElements(results.getTotalElements())
//                .totalPages(results.getTotalPages())
//                .currentPage(results.getNumber())
//                .pageSize(results.getSize())
//                .sortBy(sortBy)
//                .sortOrder(sortDir)
//                .appliedFilters(getAppliedFilters(request))
//                .build();
//    }
//
//    private List<String> getAppliedFilters(SopSearchRequest request) {
//        List<String> filters = new ArrayList<>();
//
//        if (StringUtils.hasText(request.getDepartment())) {
//            filters.add("department:" + request.getDepartment());
//        }
//        if (StringUtils.hasText(request.getCategory())) {
//            filters.add("category:" + request.getCategory());
//        }
//        if (StringUtils.hasText(request.getStatus())) {
//            filters.add("status:" + request.getStatus());
//        }
//        if (StringUtils.hasText(request.getKeyword())) {
//            filters.add("keyword:" + request.getKeyword());
//        }
//
//        return filters;
//    }
//
//    private void handleSearchException(Exception e) throws Exception {
//        if (e instanceof SopNotFoundException ||
//                e instanceof InvalidSearchParameterException) {
//            throw e;
//        }
//        if (e instanceof org.springframework.dao.DataAccessException) {
//            throw new DatabaseOperationException("Database error occurred during search", e);
//        }
//        throw new RuntimeException("An unexpected error occurred during search", e);
//    }
//
//    @CacheEvict(value = "sopSearch", allEntries = true)
//    public void clearSearchCache() {
//        log.info("Clearing search cache");
//    }
//}