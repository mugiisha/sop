package com.version_control_service.version_control_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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


}
