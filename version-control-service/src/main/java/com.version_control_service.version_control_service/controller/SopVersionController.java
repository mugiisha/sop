package com.version_control_service.version_control_service.controller;

import com.version_control_service.version_control_service.dto.SopDto;
import com.version_control_service.version_control_service.service.SopVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/sops")
public class SopVersionController {


    @Autowired
    private SopVersionService sopVersionService;

    @GetMapping
    public List<SopDto> getAllSops() {
        return sopVersionService.getAllSops();
    }

    @GetMapping("/test")
    public String getAllSop() {
        return "hello you can see me ";
    }



}
