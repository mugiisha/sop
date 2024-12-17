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
            "/api/v1/auth/password-reset",
            "/swagger-ui",
            "/v3/api-docs",
            "/api/v1/users/verify-email/**",
            "/users/api/v1/users/verify-email/**",
            "/webjars"
    );

    public static final List<String> passwordResetEndpoints = List.of(
            "/password-reset/request-otp",
            "/password-reset/verify-otp",
            "/api/v1/auth/password-reset",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/confirm-reset"
    );

    private final Map<String, String> routeRoles = Map.of(
            "/api/v1/roles", "ADMIN",
            "/api/v1/users", "ADMIN",
            "/api/v1/sops/all", "ADMIN",
            "/api/v1/users/department", "HOD",
            "/api/v1/departments", "ADMIN",
            "/api/v1/sops/create", "AUTHOR",
            "/api/v1/sops/review", "REVIEWER",
            "/api/v1/sops/approve", "APPROVER",
            "/api/v1/feedback/respond", "HOD",
            "/api/v1/feedback/delete", "HOD"

    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        boolean isOpen = isOpenEndpoint(path);
        logger.debug("Security check for path: {} - isSecured: {}", path, !isOpen);
        return !isOpen;
    };

    public boolean isOpenEndpoint(String path) {
        return openEndpoints.stream()
                .anyMatch(endpoint -> matchPath(path, endpoint));
    }

    public boolean isPasswordResetEndpoint(String path) {
        return passwordResetEndpoints.stream()
                .anyMatch(endpoint -> matchPath(path, endpoint));
    }

    public String getRequiredRole(String path) {
        for (Map.Entry<String, String> entry : routeRoles.entrySet()) {
            if (matchPath(path, entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean hasRequiredRole(String path, String userRole) {
        // Skip role check for password reset endpoints
        if (isPasswordResetEndpoint(path)) {
            logger.debug("Skipping role check for password reset endpoint: {}", path);
            return true;
        }

        // Get required role for the path
        String requiredRole = getRequiredRole(path);
        if (requiredRole == null) {
            logger.debug("No specific role required for path: {}", path);
            return true;
        }

        boolean hasRole = requiredRole.equals(userRole);
        logger.debug("Role check for path: {} - Required: {} - User: {} - Result: {}",
                path, requiredRole, userRole, hasRole);

        return hasRole;
    }

    private boolean matchPath(String path, String pattern) {
        // Handle wildcard patterns
        if (pattern.endsWith("/**")) {
            String basePattern = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(basePattern);
        }
        // Simple contains check for non-wildcard patterns
        return path.contains(pattern);
    }

    public String getSecurityLevel(String path) {
        if (isOpenEndpoint(path)) {
            return "PUBLIC";
        } else if (isPasswordResetEndpoint(path)) {
            return "PASSWORD_RESET";
        } else if (getRequiredRole(path) != null) {
            return "ROLE_PROTECTED";
        } else {
            return "AUTHENTICATED";
        }
    }
}