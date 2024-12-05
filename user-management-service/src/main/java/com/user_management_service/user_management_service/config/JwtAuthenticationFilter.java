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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityProperties securityProperties;
    private final Cache tokenBlacklistCache;
    private final Cache userAuthCache;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Lazy UserService userService,
            SecurityProperties securityProperties,
            CacheManager cacheManager) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.securityProperties = securityProperties;

        // Initialize local caches
        this.tokenBlacklistCache = cacheManager.getCache("tokenBlacklist");
        this.userAuthCache = cacheManager.getCache("userAuth");
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

            // Check token blacklist from cache
            if (isTokenBlacklisted(jwt)) {
                log.warn("Attempted to use blacklisted token");
                filterChain.doFilter(request, response);
                return;
            }

            // Extract token claims
            final String userIdStr = jwtService.extractUserId(jwt);
            final UUID userId = UUID.fromString(userIdStr);
            final String email = jwtService.extractEmail(jwt);
            final String role = jwtService.extractRole(jwt);
            final UUID departmentId = UUID.fromString(jwtService.extractDepartmentId(jwt));

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Try to get user authentication from cache
                UsernamePasswordAuthenticationToken cachedAuth = getUserAuthFromCache(userId);

                if (cachedAuth != null && jwtService.isTokenValid(jwt)) {
                    SecurityContextHolder.getContext().setAuthentication(cachedAuth);
                    setRequestAttributes(request, userId, email, role, departmentId);
                    log.debug("Using cached authentication for user: {}", email);
                } else {
                    authenticateUser(request, userId, email, role, departmentId, jwt);
                }
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklistCache.get(token) != null;
    }

    private UsernamePasswordAuthenticationToken getUserAuthFromCache(UUID userId) {
        return userAuthCache.get(userId, UsernamePasswordAuthenticationToken.class);
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

            // Cache the authentication token
            userAuthCache.put(userId, authToken);

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

    public void blacklistToken(String token) {
        tokenBlacklistCache.put(token, Boolean.TRUE);
    }

    public void clearUserAuthCache(UUID userId) {
        userAuthCache.evict(userId);
    }
}