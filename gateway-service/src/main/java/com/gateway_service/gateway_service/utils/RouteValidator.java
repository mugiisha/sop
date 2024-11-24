package com.gateway_service.gateway_service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    private static final Logger logger = LoggerFactory.getLogger(RouteValidator.class);

    public static final List<String> openEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/password-reset/request-otp",
            "/password-reset/verify-otp",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );

    // map of routes to required roles
    private final Map<String, String> routeRoles = Map.of(
            "/api/v1/users/register", "ADMIN",
            "/api/v1/departments/create", "ADMIN",
                "/api/v1/roles", "ADMIN"
            // Add here all routes you are working on that require specific roles
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();
                boolean isOpen = openEndpoints.stream()
                        .anyMatch(path::contains);

                logger.debug("Checking security for path: {} - isSecured: {}", path, !isOpen);
                return !isOpen;
            };


//     Get required role for a specific path
    public String getRequiredRole(String path) {
        for (Map.Entry<String, String> entry : routeRoles.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }


//     Check if user's role matches the required role for the path

    public boolean hasRequiredRole(String path, String userRole) {
        String requiredRole = getRequiredRole(path);
        if (requiredRole == null) {
            return true; // No specific role required
        }
        return requiredRole.equals(userRole);
    }
}