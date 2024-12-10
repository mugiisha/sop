package com.sop_content_service.sop_content_service.strategy;

import com.sop_content_service.sop_content_service.dto.SopSearchRequest;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.SopRepositorySearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SimpleSearchStrategy implements SearchStrategy {
    private final SopRepositorySearch repository;

    public SimpleSearchStrategy(SopRepositorySearch repository) {
        this.repository = repository;
    }

    @Override
    public Page<SopModel> search(SopSearchRequest request, Pageable pageable) {
        if (StringUtils.hasText(request.getStatus())) {
            return repository.findByVisibility(request.getStatus(), pageable);  // Changed from findByStatus to findByVisibility
        }
        if (StringUtils.hasText(request.getCategory())) {
            return repository.findByCategoryId(request.getCategory(), pageable);
        }
        return Page.empty(pageable);
    }
}