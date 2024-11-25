package com.gateway_service.gateway_service.filters;

import com.gateway_service.gateway_service.utils.RouteValidator;
import com.gateway_service.gateway_service.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final RouteValidator validator;
    private final JwtUtils jwtUtils;

    public AuthFilter(RouteValidator validator, JwtUtils jwtUtils) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            if (validator.isSecured.test(request)) {
                try {
                    String token = extractToken(request);
                    if (token == null) {
                        return onError(exchange, HttpStatus.UNAUTHORIZED, "Login to perform this action");
                    }

                    if (!jwtUtils.isTokenValid(token)) {
                        return onError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized to perform this action");
                    }

                    // Extract role and check permissions
                    String userRole = jwtUtils.extractRole(token);
                    if (!validator.hasRequiredRole(path, userRole)) {
                        return onError(exchange, HttpStatus.FORBIDDEN, "You don't have permission to perform this action");
                    }

                    // Add user information to request headers
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", jwtUtils.extractUserId(token))
                            .header("X-User-Role", userRole)
                            .header("X-Department-Id", jwtUtils.extractDepartmentId(token))
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } catch (Exception e) {
                    return onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication processing error");
                }
            }

            return chain.filter(exchange);
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String errorBody = String.format("{\"message\": \"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
    }

    public static class Config {
        // Add configuration properties if needed
    }
}