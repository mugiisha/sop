package com.user_management_service.user_management_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
class TestConfig {
    @Bean
    public SecurityProperties securityProperties() {
        SecurityProperties properties = new SecurityProperties();
        properties.setPublicPaths("/api/v1/auth/**,/api/v1/public/**");
        properties.setSwaggerPaths("/swagger-ui/**,/v3/api-docs/**");
        return properties;
    }
}
