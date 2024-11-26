package com.version_control_service.version_control_service.controller;

import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.dto.SopDto;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.service.SopVersionGrpcService;
import com.version_control_service.version_control_service.service.SopVersionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@RestController
@RequestMapping("/api/sop-versions")
public class SopVersionController {

    private static final Logger logger = LoggerFactory.getLogger(SopVersionController.class);

    @Autowired
    private SopVersionGrpcService SopVersionGrpcService;

    @Autowired
    private SopVersionService sopVersionService;

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

    // Endpoint to initialize a new SOP version
    @PostMapping("/create")
    public ApiResponse<SopVersionModel> initializeSopVersion(@Valid @RequestBody SopVersionModel sopVersionModel) {
        return sopVersionService.initializeSopVersion(sopVersionModel);
    }

    // Endpoint to fetch all versions for a specific SOP ID
    @GetMapping("/by-sop-id/{sopId}")
    public ApiResponse<List<SopVersionModel>> getVersionsBySopId(@PathVariable String sopId) {
        return sopVersionService.getVersionsBySopId(sopId);
    }

    // Endpoint to fetch the latest version of a specific SOP
    @GetMapping("/latest/{sopId}")
    public ApiResponse<SopVersionModel> getLatestVersion(@PathVariable String sopId) {
        return sopVersionService.getLatestVersion(sopId);
    }

    // Endpoint to fetch a specific version of an SOP
    @GetMapping("/{sopId}/{versionNumber}")
    public ApiResponse<SopVersionModel> getSpecificVersion(@PathVariable String sopId, @PathVariable String versionNumber) {
        return sopVersionService.getSpecificVersion(sopId, versionNumber);
    }



}
