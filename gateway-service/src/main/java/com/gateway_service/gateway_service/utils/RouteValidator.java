package com.gateway_service.gateway_service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
public class RouteValidator {
    private static final Logger logger = LoggerFactory.getLogger(RouteValidator.class);

    // Convert endpoints with wildcards to regex patterns
    private static final List<Pattern> OPEN_ENDPOINT_PATTERNS = Arrays.asList(
            Pattern.compile("^/auth/register$"),
            Pattern.compile("^/auth/login$"),
            Pattern.compile("^/password-reset/request-otp$"),
            Pattern.compile("^/password-reset/verify-otp$"),
            Pattern.compile("^/swagger-ui.*$"),
            Pattern.compile("^/v3/api-docs.*$"),
            Pattern.compile("^/api/v1/users/verify-email/.*$"),
            Pattern.compile("^/webjars.*$")
    );

    // Use exact path matching for role-based routes
    private final Map<Pattern, String> routeRolePatterns;

    public RouteValidator() {
        Map<Pattern, String> patterns = new HashMap<>();
        patterns.put(Pattern.compile("^/api/v1/roles$"), "ADMIN");
        patterns.put(Pattern.compile("^/api/v1/users$"), "ADMIN");
        patterns.put(Pattern.compile("^/api/v1/sops/all$"), "ADMIN");
        patterns.put(Pattern.compile("^/api/v1/users/department$"), "HOD");
        patterns.put(Pattern.compile("^/api/v1/departments$"), "ADMIN");
        patterns.put(Pattern.compile("^/api/v1/sops/create$"), "AUTHOR");
        patterns.put(Pattern.compile("^/api/v1/sops/review$"), "REVIEWER");
        patterns.put(Pattern.compile("^/api/v1/sops/approve$"), "APPROVER");
        patterns.put(Pattern.compile("^/api/v1/feedback/respond$"), "HOD");
        patterns.put(Pattern.compile("^/api/v1/feedback/delete$"), "HOD");
        this.routeRolePatterns = Collections.unmodifiableMap(patterns);
    }

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = normalizePath(request.getURI().getPath());
                boolean isOpen = OPEN_ENDPOINT_PATTERNS.stream()
                        .anyMatch(pattern -> pattern.matcher(path).matches());

                logger.debug("Checking security for path: {} - isSecured: {}", path, !isOpen);
                return !isOpen;
            };

    public String getRequiredRole(String path) {
        String normalizedPath = normalizePath(path);
        return routeRolePatterns.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(normalizedPath).matches())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public boolean hasRequiredRole(String path, String userRole) {
        if (userRole == null) {
            return false; // Never allow null roles
        }

        String requiredRole = getRequiredRole(path);
        if (requiredRole == null) {
            return true; // No specific role required
        }
        return requiredRole.equals(userRole);
    }

    private String normalizePath(String path) {
        // Remove trailing slash if present, except for root path
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }
}