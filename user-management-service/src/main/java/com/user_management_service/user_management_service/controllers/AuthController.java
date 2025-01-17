package com.user_management_service.user_management_service.controllers;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication operations including login, registration, password reset, and logout")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and get token",
            description = "Authenticates user credentials and returns a JWT token along with user details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account locked or email not verified"
            )
    })
    public ResponseEntity<ApiResponseWrapper<LoginResponseDTO>> login(
            @Valid @RequestBody UserLoginDTO loginDTO) {
        LoginResponseDTO loginResponse = authService.login(loginDTO);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        200,
                        "Login successful",
                        loginResponse
                )
        );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user and invalidate token",
            description = "Invalidates the current JWT token and logs out the user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token"
            )
    })
    public ResponseEntity<ApiResponseWrapper<Void>> logout(
            @RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7); // Remove "Bearer " prefix
        authService.logout(token);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        200,
                        "Logout successful",
                        null
                )
        );
    }

    @PostMapping("/password-reset/request-otp")
    @Operation(
            summary = "Request password reset OTP",
            description = "Sends a one-time password to the user's email for password reset"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponseWrapper<OtpResponseDTO>> requestPasswordReset(
            @Valid @RequestBody EmailRequestDTO requestDTO) {
        OtpResponseDTO response = authService.requestPasswordReset(requestDTO);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        200,
                        "OTP sent successfully",
                        response
                )
        );
    }

    @PostMapping("/password-reset/verify-otp")
    @Operation(
            summary = "Verify OTP and get reset token",
            description = "Verifies the OTP and returns a token for password reset"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP verified successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponseWrapper<TokenResponseDTO>> verifyOtp(
            @Valid @RequestBody OtpVerificationDTO verificationDTO) {
        String token = authService.verifyOtpAndGenerateToken(verificationDTO);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        200,
                        "OTP verified successfully",
                        new TokenResponseDTO(token)
                )
        );
    }

    @PostMapping("/password-reset")
    @Operation(
            summary = "Reset password using token",
            description = "Resets the user's password using the token received after OTP verification",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successful",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or passwords don't match"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired reset token"
            )
    })
    public ResponseEntity<ApiResponseWrapper<Void>> resetPassword(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody PasswordResetDTO resetDTO) {
        String token = bearerToken.substring(7);
        authService.resetPassword(token, resetDTO);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        200,
                        "Password reset successfully",
                        null
                )
        );
    }

    @GetMapping("/verify-email/{token}")
    @Operation(
            summary = "Verify email address",
            description = "Verifies the user's email address using the verification token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token"
            )
    })
    public ResponseEntity<ApiResponseWrapper<Void>> verifyEmail(
            @PathVariable String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        200,
                        "Email verified successfully",
                        null
                )
        );
    }
}