package com.version_control_service.version_control_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableMongoAuditing
public class VersionControlServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Harare"));
        SpringApplication.run(VersionControlServiceApplication.class, args);
    }

}