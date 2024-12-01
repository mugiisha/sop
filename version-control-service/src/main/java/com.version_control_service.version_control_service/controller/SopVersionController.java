package com.version_control_service.version_control_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.dto.SopDto;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.service.SopVersionGrpcService;
import com.version_control_service.version_control_service.service.SopVersionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @PostMapping("/create")
    public List<SopDto> displayAllSops() {
        logger.info("Entering displayAllSops() method via POST /create");
        List<SopDto> sops = null;
        try {
            logger.debug("Calling sopVersionService.getAllSops() from displayAllSops()");
            sops = SopVersionGrpcService.getAllSops();
            logger.info("Successfully retrieved SOPs: {}", sops);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SOPs", e);
        }
            logger.info("Exiting displayAllSops() method");
            return sops;
    }


    @PostMapping("/{sopId}/create")
    public SopVersionModel createNewSopVersion(
            @PathVariable String sopId,
            @RequestBody @Valid SopDto newVersionDetails) {
        logger.info("Entering createNewSopVersion() method for SOP ID: {}", sopId);
        SopVersionModel createdVersion;

        try {
            logger.debug("Calling sopVersionService.createNewSopVersion()");
            createdVersion = sopVersionService.createNewSopVersion(newVersionDetails, sopId);
            logger.info("Successfully created SOP version: {}", createdVersion);
        } catch (Exception e) {
            logger.error("Error occurred while creating SOP version", e);
            throw e; // Optionally, handle the exception and return a meaningful response
        }

        logger.info("Exiting createNewSopVersion() method");
        return createdVersion;
    }
    @GetMapping("/{versionId}")
    public ResponseEntity<SopVersionModel> getSopVersionById(@PathVariable String versionId) {
        logger.info("Entering getSopVersionById() method with versionId: {}", versionId);
        try {
            SopVersionModel sopVersion = sopVersionService.getSopVersionById(versionId);
            logger.info("Successfully retrieved SOP version: {}", sopVersion);
            return ResponseEntity.ok(sopVersion);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SOP version by versionId: {}", versionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/by-sop/{sopId}")
    public ResponseEntity<List<SopVersionModel>> getAllVersionsBySopId(@PathVariable String sopId) {
        logger.info("Entering getAllVersionsBySopId() method with sopId: {}", sopId);
        try {
            List<SopVersionModel> sopVersions = sopVersionService.getAllVersionsBySopId(sopId);
            logger.info("Successfully retrieved SOP versions for sopId: {}", sopId);
            return ResponseEntity.ok(sopVersions);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SOP versions by sopId: {}", sopId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



}
