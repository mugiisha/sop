package com.analytics_insights_service.analytics_insights_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");


        return "analytic-insights-service is reachable through the gateway!"+userId;
    }


}
