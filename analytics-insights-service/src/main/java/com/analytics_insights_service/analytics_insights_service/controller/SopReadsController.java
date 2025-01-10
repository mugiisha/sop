package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.model.SopReads;
import com.analytics_insights_service.analytics_insights_service.service.SopReadsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sop-reads")
public class SopReadsController {
    
    private final SopReadsService sopReadsService;

    public SopReadsController(SopReadsService sopReadsService) {
        this.sopReadsService = sopReadsService;
    }

    @GetMapping("/{sopId}")
    public ResponseEntity<ApiResponse<SopReads>> getSopReads(@PathVariable String sopId) {
        return ResponseEntity.ok(new ApiResponse<>("sop reads retrieved successfully",sopReadsService.getSopReads(sopId)));
    }
}
