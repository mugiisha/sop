package com.sop_content_service.sop_content_service.controller;

import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.service.SopInitializeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sop")
public class SopInitializationController {

    @Autowired
    private SopInitializeService sopInitializeService;

    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<SopModel>> initializeSOP(@RequestBody SopModel sopModel) {
        // Call the service method to initialize the SOP
        return sopInitializeService.initializeSOP(sopModel);
    }
}
