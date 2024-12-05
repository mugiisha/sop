package com.user_management_service.user_management_service.config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Arrays;

@Getter
@ConfigurationProperties(prefix = "security")
@Component
@Slf4j
public class SecurityProperties {
    private List<String> publicPaths;
    private List<String> swaggerPaths;

    public void setPublicPaths(String publicPathsString) {
        this.publicPaths = Arrays.asList(publicPathsString.split(","));
    }

    public void setSwaggerPaths(String swaggerPathsString) {
        this.swaggerPaths = Arrays.asList(swaggerPathsString.split(","));
    }
}