package com.gateway_service.gateway_service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway_service.gateway_service.utils.RouteValidator;
import com.gateway_service.gateway_service.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
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
import java.util.UUID;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
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
            String path = trimPath(request.getURI().getPath());
            String requestId = generateRequestId();

            log.debug("Processing request [{}] for path: {}", requestId, path);

            // Check if the route requires authentication
            if (routeValidator.isSecured.test(request)) {
                return handleSecuredRoute(exchange, chain, request, path, requestId);
            }

            // Add request ID to non-secured routes as well
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(REQUEST_ID_HEADER, requestId)
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> handleSecuredRoute(ServerWebExchange exchange, GatewayFilterChain chain,
                                          ServerHttpRequest request, String path, String requestId) {
        try {
            // Extract and validate the token
            String token = extractToken(request);
            if (token == null) {
                log.warn("No token provided for secured endpoint [{}]: {}", requestId, path);
                return createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authentication token is required");
            }

            // Validate the token first
            if (!jwtUtils.isTokenValid(token)) {
                log.warn("Invalid token provided for request [{}] path: {}", requestId, path);
                return createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }

            String tokenType = jwtUtils.extractTokenType(token);

            if (!"PASSWORD_RESET".equals(tokenType)) {
                String userRole = jwtUtils.extractRole(token);
                if (!routeValidator.hasRequiredRole(path, userRole)) {
                    log.warn("Insufficient permissions for user with role {} on request [{}] path: {}",
                            userRole, requestId, path);
                    return createErrorResponse(exchange, HttpStatus.FORBIDDEN, "Insufficient permissions");
                }
            }



            // Create modified request with additional headers
            ServerHttpRequest modifiedRequest = enrichRequestWithHeaders(request, token, requestId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Error processing authentication for request [{}]", requestId, e);
            return createErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Authentication processing error: " + e.getMessage());
        }
    }

    private ServerHttpRequest enrichRequestWithHeaders(ServerHttpRequest request, String token, String requestId) {
        ServerHttpRequest.Builder builder = request.mutate()
                .header("X-User-Id", jwtUtils.extractUserId(token))
                .header("X-Token-Type", jwtUtils.extractTokenType(token))
                .header(REQUEST_ID_HEADER, requestId);

        String tokenType = jwtUtils.extractTokenType(token);

        // Add role and department ID for ACCESS tokens
        if ("ACCESS".equals(tokenType)) {
            String role = jwtUtils.extractRole(token);
            String departmentId = jwtUtils.extractDepartmentId(token);
            String email = jwtUtils.extractEmail(token);
            if (role != null) {
                builder.header("X-User-Role", role);
            }
            if (departmentId != null) {
                builder.header("X-Department-Id", departmentId);
            }
            if (email != null) {
                builder.header("X-User-Email", email);
            }
        }

        // Add email for PASSWORD_RESET tokens
        if ("PASSWORD_RESET".equals(tokenType)) {
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

    private String generateRequestId() {
        return UUID.randomUUID().toString();
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
        errorBody.put("requestId", exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER));

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating error response", e);
            return Mono.error(e);
        }
    }


    private String trimPath(String path) {
        int secondSlashIndex = path.indexOf("/", path.indexOf("/") + 1);
        return (secondSlashIndex != -1) ? path.substring(secondSlashIndex) : path;
    }


    public static class Config {
        private boolean includeDebugInfo = false;

        public boolean isIncludeDebugInfo() {
            return includeDebugInfo;
        }

        public void setIncludeDebugInfo(boolean includeDebugInfo) {
            this.includeDebugInfo = includeDebugInfo;
        }
    }
}