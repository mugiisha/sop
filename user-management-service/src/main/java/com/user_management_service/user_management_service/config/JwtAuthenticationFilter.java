package com.user_management_service.user_management_service.config;

import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.services.JwtService;
import com.user_management_service.user_management_service.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityProperties securityProperties;
    private final CacheManager cacheManager;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Lazy UserService userService,
            SecurityProperties securityProperties,
            CacheManager cacheManager) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.securityProperties = securityProperties;
        this.cacheManager = cacheManager;
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
            final String jwt;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);

            // Check token blacklist
            if (isTokenBlacklisted(jwt)) {
                log.warn("Attempted to use blacklisted token");
                filterChain.doFilter(request, response);
                return;
            }

            final String userIdStr = jwtService.extractUserId(jwt);
            if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId = UUID.fromString(userIdStr);

                // Try to get cached authentication
                Optional<UsernamePasswordAuthenticationToken> cachedAuth = getCachedAuthentication(userId);

                if (cachedAuth.isPresent() && jwtService.isTokenValid(jwt)) {
                    SecurityContextHolder.getContext().setAuthentication(cachedAuth.get());
                    setRequestAttributes(request, jwt);
                    log.debug("Using cached authentication for userId: {}", userId);
                } else {
                    authenticateUser(request, jwt);
                }
            }

        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Optional<UsernamePasswordAuthenticationToken> getCachedAuthentication(UUID userId) {
        try {
            Cache userAuthCache = cacheManager.getCache("userAuth");
            if (userAuthCache != null) {
                return Optional.ofNullable(userAuthCache.get(userId.toString(),
                        UsernamePasswordAuthenticationToken.class));
            }
        } catch (Exception e) {
            log.error("Cache retrieval error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private void authenticateUser(HttpServletRequest request, String jwt) {
        try {
            String userIdStr = jwtService.extractUserId(jwt);
            UUID userId = UUID.fromString(userIdStr);
            UserResponseDTO user = userService.getUserById(userId);

            if (user != null && jwtService.isTokenValid(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Cache the authentication
                Cache userAuthCache = cacheManager.getCache("userAuth");
                if (userAuthCache != null) {
                    userAuthCache.put(userId.toString(), authToken);
                }

                SecurityContextHolder.getContext().setAuthentication(authToken);
                setRequestAttributes(request, jwt);
                log.debug("Authentication successful for userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("User authentication error: {}", e.getMessage());
        }
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            Cache blacklistCache = cacheManager.getCache("tokenBlacklist");
            return blacklistCache != null && blacklistCache.get(token) != null;
        } catch (Exception e) {
            log.error("Blacklist check error: {}", e.getMessage());
            return false;
        }
    }

    private void setRequestAttributes(HttpServletRequest request, String jwt) {
        request.setAttribute("userId", jwtService.extractUserId(jwt));
        request.setAttribute("userEmail", jwtService.extractEmail(jwt));
        request.setAttribute("userRole", jwtService.extractRole(jwt));
        request.setAttribute("departmentId", jwtService.extractDepartmentId(jwt));
    }

    public void blacklistToken(String token) {
        try {
            Cache blacklistCache = cacheManager.getCache("tokenBlacklist");
            if (blacklistCache != null) {
                blacklistCache.put(token, true);
                log.debug("Token blacklisted successfully");
            }
        } catch (Exception e) {
            log.error("Token blacklisting error: {}", e.getMessage());
        }
    }

    public void clearUserAuthCache(UUID userId) {
        try {
            Cache userAuthCache = cacheManager.getCache("userAuth");
            if (userAuthCache != null) {
                userAuthCache.evict(userId.toString());
                log.debug("User auth cache cleared for userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Cache clearing error: {}", e.getMessage());
        }
    }
}