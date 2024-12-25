package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    private final String secretKey;
    private final long jwtExpiration;
    private final long resetTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.reset.expiration}") long resetTokenExpiration) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
        this.resetTokenExpiration = resetTokenExpiration;
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("departmentId", user.getDepartment().getId().toString());
        claims.put("tokenType", "ACCESS");
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generatePasswordResetToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("tokenType", "PASSWORD_RESET");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + resetTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validatePasswordResetTokenAndGetEmail(String token) {
        try {
            Claims claims = extractAllClaims(token);

            String tokenType = claims.get("tokenType", String.class);
            if (!"PASSWORD_RESET".equals(tokenType)) {
                log.warn("Invalid token type for password reset");
                return null;
            }

            if (claims.getExpiration().before(new Date())) {
                log.warn("Password reset token has expired");
                return null;
            }

            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("Error validating password reset token", e);
            return null;
        }
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractDepartmentId(String token) {
        return extractClaim(token, claims -> claims.get("departmentId", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("tokenType", String.class);

            if (tokenType == null) {
                log.warn("Token type is missing");
                return false;
            }

            return switch (tokenType) {
                case "ACCESS" -> validateAccessToken(claims);
                case "PASSWORD_RESET" -> validatePasswordResetToken(claims);
                case "REFRESH" -> validateRefreshToken(claims);
                default -> {
                    log.warn("Invalid token type: {}", tokenType);
                    yield false;
                }
            };
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired");
            return false;
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }

    private boolean validateAccessToken(Claims claims) {
        if (isTokenExpired(claims)) {
            log.warn("Access token has expired");
            return false;
        }

        String role = claims.get("role", String.class);
        if (role == null) {
            log.warn("Access token missing role claim");
            return false;
        }

        return true;
    }

    private boolean validatePasswordResetToken(Claims claims) {
        if (isTokenExpired(claims)) {
            log.warn("Password reset token has expired");
            return false;
        }

        String email = claims.get("email", String.class);
        String sub = claims.getSubject();

        if (email == null || sub == null) {
            log.warn("Password reset token missing required claims");
            return false;
        }

        return true;
    }

    private boolean validateRefreshToken(Claims claims) {
        if (isTokenExpired(claims)) {
            log.warn("Refresh token has expired");
            return false;
        }

        String sub = claims.getSubject();
        if (sub == null) {
            log.warn("Refresh token missing subject claim");
            return false;
        }

        return true;
    }

    private boolean isTokenExpired(Claims claims) {
        try {
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }

}