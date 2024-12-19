package com.gateway_service.gateway_service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtUtils {

    private final String secretKey;
    private final long jwtExpiration;
    private final long resetTokenExpiration;

    public JwtUtils(
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
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Token has expired");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Malformed token");
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid token signature");
            throw e;
        } catch (Exception e) {
            log.error("Error parsing token", e);
            throw e;
        }
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

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}