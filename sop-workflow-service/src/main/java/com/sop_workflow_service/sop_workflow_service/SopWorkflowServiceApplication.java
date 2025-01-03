package com.sop_workflow_service.sop_workflow_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SopWorkflowServiceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Harare"));
		SpringApplication.run(SopWorkflowServiceApplication.class, args);
	}
}