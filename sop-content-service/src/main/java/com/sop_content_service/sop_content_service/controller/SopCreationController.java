package com.sop_content_service.sop_content_service.controller;

import static com.sop_content_service.sop_content_service.util.validateSopRequest.validate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.dto.SopRequest;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.service.SopService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;



@RestController
@RequestMapping("/api/v1/sop")
public class SopCreationController {

    private static final Logger log = LoggerFactory.getLogger(SopCreationController.class);

    private final SopService sopService;
    private final ObjectMapper objectMapper;



    public SopCreationController(SopService sopService, ObjectMapper objectMapper) {
        this.sopService = sopService;
        this.objectMapper = objectMapper;

    }

    @PutMapping(value = "/create/{sopId}", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<SopModel>> updateSop(
            @Valid
            @PathVariable String sopId,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart("sop") String sopJson) throws IOException {

        SopRequest sopRequest = objectMapper.readValue(sopJson, SopRequest.class);

        validate(sopRequest);

        SopModel updatedSop = sopService.createSop(sopId, documents, coverImage, sopRequest);
        ApiResponse<SopModel> response = new ApiResponse<>("SOP Created successfully", updatedSop);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }




}

