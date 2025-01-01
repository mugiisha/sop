package com.role_access_control_service.role_access_control_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class RoleAccessControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoleAccessControlServiceApplication.class, args);
	}

}