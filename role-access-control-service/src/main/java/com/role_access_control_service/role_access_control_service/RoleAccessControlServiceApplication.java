package com.role_access_control_service.role_access_control_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RoleAccessControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoleAccessControlServiceApplication.class, args);
	}

}