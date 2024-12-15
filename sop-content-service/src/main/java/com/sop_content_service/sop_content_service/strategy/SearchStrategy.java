package com.sop_content_service.sop_content_service.strategy;

import com.sop_content_service.sop_content_service.dto.SopSearchRequest;
import com.sop_content_service.sop_content_service.model.Sop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchStrategy {
    Page<Sop> search(SopSearchRequest request, Pageable pageable);
}