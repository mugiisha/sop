package com.analytics_insights_service.analytics_insights_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableDiscoveryClient
@Configuration
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.analytics_insights_service.analytics_insights_service.repository")
public class AnalyticsInsightsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsInsightsServiceApplication.class, args);
	}

}
