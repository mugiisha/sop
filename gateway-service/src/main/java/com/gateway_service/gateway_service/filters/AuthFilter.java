package com.gateway_service.gateway_service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway_service.gateway_service.utils.RouteValidator;
import com.gateway_service.gateway_service.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RouteValidator routeValidator;
    private final JwtUtils jwtUtils;

    public AuthFilter(RouteValidator routeValidator, JwtUtils jwtUtils) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("Processing request for path: {}", path);

            // Check if the route requires authentication
            if (routeValidator.isSecured.test(request)) {
                try {
                    // Extract and validate the token
                    String token = extractToken(request);
                    if (token == null) {
                        log.warn("No token provided for secured endpoint: {}", path);
                        return createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authentication token is required");
                    }

                    // Validate the token
                    if (!jwtUtils.isTokenValid(token)) {
                        log.warn("Invalid token provided for path: {}", path);
                        return createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                    }

                    // Handle different token types
                    String tokenType = jwtUtils.extractTokenType(token);
                    if (!isValidTokenTypeForPath(tokenType, path)) {
                        log.warn("Invalid token type {} for path: {}", tokenType, path);
                        return createErrorResponse(exchange, HttpStatus.FORBIDDEN, "Invalid token type for this operation");
                    }

                    // Check permissions for non-password-reset endpoints
                    if (!"PASSWORD_RESET".equals(tokenType)) {
                        String userRole = jwtUtils.extractRole(token);
                        if (!routeValidator.hasRequiredRole(path, userRole)) {
                            log.warn("Insufficient permissions for user with role {} on path: {}", userRole, path);
                            return createErrorResponse(exchange, HttpStatus.FORBIDDEN, "Insufficient permissions");
                        }
                    }

                    // Create modified request with additional headers
                    ServerHttpRequest modifiedRequest = enrichRequestWithHeaders(request, token);
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    log.error("Error processing authentication", e);
                    return createErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication processing error");
                }
            }

            // Allow the request to proceed for non-secured routes
            return chain.filter(exchange);
        };
    }

    private boolean isValidTokenTypeForPath(String tokenType, String path) {
        if (routeValidator.isPasswordResetEndpoint(path)) {
            return "PASSWORD_RESET".equals(tokenType);
        }
        return "ACCESS".equals(tokenType);
    }

    private ServerHttpRequest enrichRequestWithHeaders(ServerHttpRequest request, String token) {
        ServerHttpRequest.Builder builder = request.mutate()
                .header("X-User-Id", jwtUtils.extractUserId(token))
                .header("X-Token-Type", jwtUtils.extractTokenType(token));

        // Add role and department ID for ACCESS tokens
        if ("ACCESS".equals(jwtUtils.extractTokenType(token))) {
            String role = jwtUtils.extractRole(token);
            String departmentId = jwtUtils.extractDepartmentId(token);
            if (role != null) {
                builder.header("X-User-Role", role);
            }
            if (departmentId != null) {
                builder.header("X-Department-Id", departmentId);
            }
        }

        // Add email for PASSWORD_RESET tokens
        if ("PASSWORD_RESET".equals(jwtUtils.extractTokenType(token))) {
            String email = jwtUtils.extractEmail(token);
            if (email != null) {
                builder.header("X-User-Email", email);
            }
        }

        return builder.build();
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private Mono<Void> createErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", message);
        errorBody.put("path", exchange.getRequest().getURI().getPath());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating error response", e);
            return Mono.error(e);
        }
    }

    public static class Config {
        // Configuration properties can be added here if needed
        private boolean includeDebugInfo = false;

        public boolean isIncludeDebugInfo() {
            return includeDebugInfo;
        }

        public void setIncludeDebugInfo(boolean includeDebugInfo) {
            this.includeDebugInfo = includeDebugInfo;
        }
    }
}