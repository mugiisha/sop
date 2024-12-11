package com.user_management_service.user_management_service.config;

import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.exceptions.AuthenticationException;
import com.user_management_service.user_management_service.services.JwtService;
import com.user_management_service.user_management_service.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityProperties securityProperties;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Lazy UserService userService,
            SecurityProperties securityProperties) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.securityProperties = securityProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return securityProperties.getPublicPaths().stream().anyMatch(path::startsWith) ||
                securityProperties.getSwaggerPaths().stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(BEARER_PREFIX.length());

            // Validate token first
            if (!jwtService.isTokenValid(jwt)) {
                throw new AuthenticationException("Invalid or expired token");
            }

            // Extract token claims
            final String userIdStr = jwtService.extractUserId(jwt);
            final UUID userId = UUID.fromString(userIdStr);
            final String email = jwtService.extractEmail(jwt);
            final String role = jwtService.extractRole(jwt);
            final String departmentIdStr = jwtService.extractDepartmentId(jwt);
            final UUID departmentId = departmentIdStr != null ? UUID.fromString(departmentIdStr) : null;

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(request, userId, email, role, departmentId, jwt);
                log.debug("Successfully authenticated user [{}]. UserId: {}, Email: {}, Role: {}",
                        requestId, userId, email, role);
            }

        } catch (AuthenticationException e) {
            log.error("Authentication failed for request [{}]: {}", requestId, e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed", e.getMessage(), requestId);
            return;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for request [{}]", requestId, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error",
                    "An unexpected error occurred during authentication", requestId);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(
            HttpServletRequest request,
            UUID userId,
            String email,
            String role,
            UUID departmentId,
            String jwt
    ) {
        UserResponseDTO user = userService.getUserById(userId);

        if (user == null) {
            throw new AuthenticationException("User not found");
        }

        UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(user, request);
        setRequestAttributes(request, userId, email, role, departmentId);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            UserResponseDTO user,
            HttpServletRequest request
    ) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }

    private void setRequestAttributes(
            HttpServletRequest request,
            UUID userId,
            String email,
            String role,
            UUID departmentId
    ) {
        request.setAttribute("userId", userId);
        request.setAttribute("userEmail", email);
        request.setAttribute("userRole", role);
        if (departmentId != null) {
            request.setAttribute("departmentId", departmentId);
        }
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            int status,
            String error,
            String message,
            String requestId
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", status);
        errorBody.put("error", error);
        errorBody.put("message", message);
        errorBody.put("requestId", requestId);

        String jsonResponse = objectMapper.writeValueAsString(errorBody);
        response.getWriter().write(jsonResponse);
    }
}