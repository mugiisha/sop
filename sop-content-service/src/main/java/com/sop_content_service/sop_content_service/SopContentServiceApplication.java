package com.sop_content_service.sop_content_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class SopContentServiceApplication {
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Harare"));
		SpringApplication.run(SopContentServiceApplication.class, args);
	}
}