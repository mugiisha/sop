package com.analytics_insights_service.analytics_insights_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient

@SpringBootApplication
public class AnalyticsInsightsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsInsightsServiceApplication.class, args);
	}

}
