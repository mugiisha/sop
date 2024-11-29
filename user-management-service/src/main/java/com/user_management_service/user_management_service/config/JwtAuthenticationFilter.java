package com.user_management_service.user_management_service.config;

import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.services.JwtService;
import com.user_management_service.user_management_service.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;
import java.io.IOException;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    // List of paths that don't require authentication
    private final List<String> publicPaths = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/password-reset/request",
            "/api/v1/users/verify-email/**",
            "/api/v1/auth/password-reset/verify",
            "/api/v1/auth/password-reset/confirm",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/password-reset",
            "/api/v1/auth/password-reset/set-password"
    );

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip JWT filter for public endpoints
        return publicPaths.stream().anyMatch(path::startsWith) ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userId = jwtService.extractUserId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserResponseDTO user = userService.getUserById(UUID.fromString(userId));

                if (jwtService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the error but don't throw it to avoid blocking the request
            logger.error("JWT Authentication failed: ", e);
        }

        filterChain.doFilter(request, response);
    }
}