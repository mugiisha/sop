package com.sop_content_service.sop_content_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.service.SopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sop")
public class SopCreationController {

    @Autowired
    private SopService sopService;

    @Autowired
    private ObjectMapper objectMapper;

    // Endpoint to create a new SOP
    @PatchMapping("/create/{sopId}")
    public ResponseEntity<ApiResponse<SopModel>> createSOP(
            @Valid
            @PathVariable String sopId,
            @RequestPart("sop") String sopJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "documentFile", required = false) MultipartFile documentFile) throws JsonProcessingException {

        // Deserialize JSON to SopModel
        SopModel sopModel = objectMapper.readValue(sopJson, SopModel.class);

        // Delegate to service and return the response
        return sopService.createSOP(sopId,sopModel, imageFile, documentFile);
    }

    @GetMapping("/get-All")
    public ResponseEntity<ApiResponse<List<SopModel>>> getAllSOPs() {
        // Delegate to the service and return the response
        return sopService.getAllSOPs();
    }

    // Endpoint to get all SOPs by status
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<SopModel>>> getSOPsByStatus(@PathVariable String status) {
        return sopService.getSOPsByStatus(status);
    }
}
