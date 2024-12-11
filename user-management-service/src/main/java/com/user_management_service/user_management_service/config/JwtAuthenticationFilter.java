package com.user_management_service.user_management_service.config;

import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.services.JwtService;
import com.user_management_service.user_management_service.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
        return securityProperties.getPublicPaths().stream().anyMatch(path::startsWith);
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
            final String userIdStr = jwtService.extractUserId(jwt);

            if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId = UUID.fromString(userIdStr);

                if (jwtService.isTokenValid(jwt)) {
                    UserResponseDTO user = userService.getUserById(userId);
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(user, request);
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Set request attributes
                        setRequestAttributes(request, jwt);
                        log.debug("Successfully authenticated user: {}", user.getEmail());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
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

    private void setRequestAttributes(HttpServletRequest request, String jwt) {
        request.setAttribute("userId", jwtService.extractUserId(jwt));
        request.setAttribute("userEmail", jwtService.extractEmail(jwt));
        request.setAttribute("userRole", jwtService.extractRole(jwt));
        request.setAttribute("departmentId", jwtService.extractDepartmentId(jwt));
    }
}