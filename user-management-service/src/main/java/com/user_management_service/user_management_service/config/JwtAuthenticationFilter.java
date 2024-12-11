package com.user_management_service.user_management_service.config;

import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.services.JwtService;
import com.user_management_service.user_management_service.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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
        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);

            // Extract token claims
            final String userIdStr = jwtService.extractUserId(jwt);
            final UUID userId = UUID.fromString(userIdStr);
            final String email = jwtService.extractEmail(jwt);
            final String role = jwtService.extractRole(jwt);
            final UUID departmentId = UUID.fromString(jwtService.extractDepartmentId(jwt));

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(request, userId, email, role, departmentId, jwt);
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
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

        if (jwtService.isTokenValid(jwt)) {
            UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(user, request);
            setRequestAttributes(request, userId, email, role, departmentId);

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Successfully authenticated user. UserId: {}, Email: {}, Role: {}",
                    userId, email, role);
        }
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
        request.setAttribute("departmentId", departmentId);
    }
}