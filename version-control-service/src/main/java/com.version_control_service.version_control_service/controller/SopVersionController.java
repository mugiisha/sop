package com.version_control_service.version_control_service.controller;

import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.dto.SopDto;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.service.SopVersionGrpcService;
import com.version_control_service.version_control_service.service.SopVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/v1/sop-versions")
public class SopVersionController {

    private static final Logger logger = LoggerFactory.getLogger(SopVersionController.class);

    @Autowired
    private SopVersionGrpcService SopVersionGrpcService;

    @Autowired
    private SopVersionService sopVersionService;

    @Autowired
    private ObjectMapper objectMapper;

    public SopVersionController(SopVersionService sopVersionService) {
        this.sopVersionService = sopVersionService;
    }

    @GetMapping
    public List<SopDto> getAllSops() {
        logger.info("Entering getAllSops() method");
        List<SopDto> sops = null;

        try {
            logger.debug("Calling sopVersionService.getAllSops()");
            sops = SopVersionGrpcService.getAllSops();
            logger.info("Successfully retrieved SOPs: {}", sops);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SOPs", e);
        }

        logger.info("Exiting getAllSops() method");
        return sops;
    }

    @PostMapping("/create/{sopId}")
    public ResponseEntity<ApiResponse<SopVersionModel>> initializeSopVersion(
            @PathVariable String sopId,
            @RequestPart("sop") String sopJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "documentFile", required = false) MultipartFile documentFile) {
        try {
            // Deserialize the JSON string into a SopVersionModel object
            SopVersionModel sopModel = objectMapper.readValue(sopJson, SopVersionModel.class);

            // Call the service method to initialize the SOP version
            ApiResponse<SopVersionModel> response = sopVersionService.initializeSopVersion(
                    sopId, sopModel, imageFile, documentFile);

            // Return the response with the appropriate HTTP status
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (IOException e) {
            // Log and handle JSON parsing errors
            logger.error("Failed to parse sopJson: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Invalid SOP data format", null));
        } catch (Exception e) {
            // Log and handle other unexpected errors
            logger.error("An error occurred while initializing SOP version: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An unexpected error occurred", null));
        }
    }


    // Endpoint to fetch all versions for a specific SOP ID
    @GetMapping("/by-sop-id/{sopId}")
    public ApiResponse<List<SopVersionModel>> getVersionsBySopId(@PathVariable String sopId) {
        return sopVersionService.getVersionsBySopId(sopId);
    }

    // Endpoint to fetch the latest version of a specific SOP
//    @GetMapping("/latest/{sopId}")
//    public ApiResponse<SopVersionModel> getLatestVersion(@PathVariable String sopId) {
//        return sopVersionService.getLatestVersion(sopId);
//    }

    // Endpoint to fetch a specific version of an SOP
    @GetMapping("/{sopId}/{versionNumber}")
    public ApiResponse<SopVersionModel> getSpecificVersion(@PathVariable String sopId, @PathVariable String versionNumber) {
        return sopVersionService.getSpecificVersion(sopId, versionNumber);
    }

    // Endpoint to fetch a specific SOP version by its versionId (as String)
//    @GetMapping("/by-version-id/{versionId}")
//    public ApiResponse<List<SopVersionModel>> getVersionById(@PathVariable String versionId) {
//        return sopVersionService.getVersionById(versionId);
//    }

}
