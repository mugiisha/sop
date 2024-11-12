package com.sop_workflow_service.sop_workflow_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "sop-workflow-service is reachable through the gateway!";
    }
}
