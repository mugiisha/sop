package com.sop_content_service.sop_content_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_content_service.sop_content_service.dto.SopRequest;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.service.SopService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sops")
public class SopCreationController {

    private static final Logger log = LoggerFactory.getLogger(SopCreationController.class);

    private final SopService sopService;
    private final ObjectMapper objectMapper;

    public SopCreationController(SopService sopService, ObjectMapper objectMapper) {
        this.sopService = sopService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<SopModel> uploadSop(
            @Valid
            @RequestPart("sop") String sopJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documentFiles
    ) {
        try {
            // Deserialize JSON to SopRequest
            SopRequest sopRequest = objectMapper.readValue(sopJson, SopRequest.class);

            // Call the service to handle the uploaded SOP
            SopModel sopModel = sopService.uploadSop(documentFiles, coverImage, sopRequest);
            return ResponseEntity.ok(sopModel);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse SOP JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalArgumentException e) {
            log.error("Error while uploading SOP: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Unexpected error while uploading SOP", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
