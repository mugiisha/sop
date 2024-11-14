package com.user_management_service.user_management_service.controllers;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication operations including login, registration, and password reset")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
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

    @PostMapping("/password-reset/request-otp")
    @Operation(summary = "Request password reset OTP")
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
    public ResponseEntity<GenericResponse<OtpResponseDTO>> requestPasswordReset(
            @Valid @RequestBody EmailRequestDTO requestDTO) {
        OtpResponseDTO response = authService.requestPasswordReset(requestDTO);
        return ResponseEntity.ok(new GenericResponse<>(
                200,
                "OTP sent successfully",
                response
        ));
    }

    @PostMapping("/password-reset/verify-otp")
    @Operation(summary = "Verify OTP and get reset token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP verified successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP"
            )
    })
    public ResponseEntity<GenericResponse<TokenResponseDTO>> verifyOtp(
            @Valid @RequestBody OtpVerificationDTO verificationDTO) {
        String token = authService.verifyOtpAndGenerateToken(verificationDTO);
        return ResponseEntity.ok(new GenericResponse<>(
                200,
                "OTP verified successfully",
                new TokenResponseDTO(token)
        ));
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Reset password using token")
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
    public ResponseEntity<GenericResponse<Void>> resetPassword(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody PasswordResetDTO resetDTO) {
        String token = bearerToken.substring(7);
        authService.resetPassword(token, resetDTO);
        return ResponseEntity.ok(new GenericResponse<>(
                200,
                "Password reset successfully",
                null
        ));
    }

    @GetMapping("/verify-email/{token}")
    @Operation(summary = "Verify email address")
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
    public ResponseEntity<GenericResponse<Void>> verifyEmail(@PathVariable String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new GenericResponse<>(
                200,
                "Email verified successfully",
                null
        ));
    }
}