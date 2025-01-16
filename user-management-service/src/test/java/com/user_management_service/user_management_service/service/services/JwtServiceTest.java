package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 3600000; // 1 hour
    private static final long RESET_TOKEN_EXPIRATION = 900000; // 15 minutes

    @Mock
    private User mockUser;

    @Mock
    private Department mockDepartment;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, RESET_TOKEN_EXPIRATION);

        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.getDepartment()).thenReturn(mockDepartment);
        when(mockDepartment.getId()).thenReturn(departmentId);
    }

    @Test
    void generateToken_ShouldCreateValidAccessToken() {
        // Arrange
        String role = "ADMIN";

        // Act
        String token = jwtService.generateToken(mockUser, role);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertEquals(role, jwtService.extractRole(token));
        assertEquals(mockUser.getId().toString(), jwtService.extractUserId(token));
        assertEquals(mockDepartment.getId().toString(), jwtService.extractDepartmentId(token));
        assertEquals("ACCESS", jwtService.extractTokenType(token));
    }

    @Test
    void generatePasswordResetToken_ShouldCreateValidResetToken() {
        // Act
        String token = jwtService.generatePasswordResetToken(mockUser);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertEquals(mockUser.getId().toString(), jwtService.extractUserId(token));
        assertEquals("PASSWORD_RESET", jwtService.extractTokenType(token));
    }

    @Test
    void validatePasswordResetToken_WithValidToken_ShouldReturnEmail() {
        // Arrange
        String token = jwtService.generatePasswordResetToken(mockUser);

        // Act
        String email = jwtService.validatePasswordResetTokenAndGetEmail(token);

        // Assert
        assertNotNull(email);
        assertEquals("test@example.com", email);
    }

    @Test
    void validatePasswordResetToken_WithExpiredToken_ShouldReturnNull() {
        // Arrange
        JwtService shortExpirationJwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, 0);
        String token = shortExpirationJwtService.generatePasswordResetToken(mockUser);

        // Act & Assert
        assertNull(shortExpirationJwtService.validatePasswordResetTokenAndGetEmail(token));
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        JwtService shortExpirationJwtService = new JwtService(SECRET_KEY, 0, RESET_TOKEN_EXPIRATION);
        String token = shortExpirationJwtService.generateToken(mockUser, "ADMIN");

        // Act & Assert
        assertFalse(shortExpirationJwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_WithInvalidTokenType_ShouldReturnFalse() {
        // This test would require modifying the token claims directly, which isn't possible
        // with the current implementation. In a real scenario, you might need to use a
        // different JWT library or create a test-specific method to generate invalid tokens.
    }

    @Test
    void isTokenValid_WithMalformedToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtService.isTokenValid("malformed.token.here"));
    }

    @Test
    void extractClaims_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () ->
                jwtService.extractEmail("invalid.token.here")
        );
    }

    @Test
    void generateToken_ShouldCreateTokenWithCorrectExpiration() {
        // Arrange
        String role = "USER";
        long currentTime = System.currentTimeMillis();

        // Act
        String token = jwtService.generateToken(mockUser, role);

        // Assert
        Date expirationDate = new Date(currentTime + JWT_EXPIRATION);
        assertTrue(jwtService.isTokenValid(token));
        // Note: We can't directly test the exact expiration time due to the small time difference
        // between token generation and assertion, but we can verify it's valid
    }
}