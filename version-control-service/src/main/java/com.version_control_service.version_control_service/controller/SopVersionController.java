package com.version_control_service.version_control_service.controller;

import com.version_control_service.version_control_service.dto.SopDto;
import com.version_control_service.version_control_service.service.SopVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@RestController
@RequestMapping("/api/sops")
public class SopVersionController {

    private static final Logger logger = LoggerFactory.getLogger(SopVersionController.class);

    @Autowired
    private SopVersionService sopVersionService;

    @GetMapping
    public List<SopDto> getAllSops() {
        logger.info("Entering getAllSops() method");
        List<SopDto> sops = null;

        try {
            logger.debug("Calling sopVersionService.getAllSops()");
            sops = sopVersionService.getAllSops();
            logger.info("Successfully retrieved SOPs: {}", sops);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving SOPs", e);
        }

        logger.info("Exiting getAllSops() method");
        return sops;
    }

    @GetMapping("/test")
    public String getAllSop() {
        return "hello you can see me ";
    }



}
