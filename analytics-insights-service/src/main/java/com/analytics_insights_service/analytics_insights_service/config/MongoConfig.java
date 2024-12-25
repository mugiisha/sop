package com.analytics_insights_service.analytics_insights_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


@EnableMongoAuditing
@Configuration
public class MongoConfig {
    @Bean
    public String test() {
        return "test";
    }
}
