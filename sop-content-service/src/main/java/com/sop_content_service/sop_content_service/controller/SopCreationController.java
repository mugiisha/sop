package com.sop_content_service.sop_content_service.controller;

import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.dto.SOPResponseDto;
import com.sop_content_service.sop_content_service.dto.SopContentDto;
import com.sop_content_service.sop_content_service.model.Sop;
import com.sop_content_service.sop_content_service.service.SopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/sops")
public class SopCreationController {

    private static final Logger log = LoggerFactory.getLogger(SopCreationController.class);

    private final SopService sopService;



    public SopCreationController(SopService sopService) {
        this.sopService = sopService;

    }

    @PutMapping(value = "/{sopId}/create", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<Sop>> updateSop(
            @PathVariable String sopId,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @Valid @RequestPart("sopContent") SopContentDto sopContentDto) throws IOException {


        Sop updatedSop = sopService.addSopContent(sopId, documents, coverImage, sopContentDto);
        ApiResponse<Sop> response = new ApiResponse<>("SOP content added successfully", updatedSop);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{sopId}/publish")
    public ResponseEntity<ApiResponse<Sop>> publishSop(@PathVariable String sopId) {
        Sop sop = sopService.publishSop(sopId);
        ApiResponse<Sop> response = new ApiResponse<>("SOP published successfully.", sop);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


//      * @return ApiResponse containing a list of SOPDto objects
    @GetMapping
    public ResponseEntity<ApiResponse<List<SOPResponseDto>>> getSops(HttpServletRequest request) {

        String departmentId = request.getHeader("X-Department-Id");

        List<SOPResponseDto> sops = sopService.getSops(UUID.fromString(departmentId));
        ApiResponse<List<SOPResponseDto>> response = new ApiResponse<>("Fetched all SOPs successfully.", sops);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    //      * @return ApiResponse containing a list of SOPDto objects
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SOPResponseDto>>> getAllSops() {
        List<SOPResponseDto> sops = sopService.getAllSops();
        ApiResponse<List<SOPResponseDto>> response = new ApiResponse<>("Fetched all SOPs successfully.", sops);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }


    /**
     * Retrieve an SOP by its ID.
     */

    @GetMapping("/{sopId}")
    public ResponseEntity<ApiResponse<SOPResponseDto>> getSopById(@PathVariable String sopId) {
        SOPResponseDto sop = sopService.getSopById(sopId);
        ApiResponse<SOPResponseDto> response = new ApiResponse<>("Fetched SOP successfully.", sop);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }





}

