package com.sop_content_service.sop_content_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing  // Enables auditing functionality in the application
public class MongoConfig {
}
