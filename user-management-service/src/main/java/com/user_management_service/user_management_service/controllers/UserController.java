package com.user_management_service.user_management_service.controllers;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations for managing users including registration, verification, and user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        return ResponseEntity.ok(userService.registerUser(registrationDTO));
    }

    // In UserController.java
    @GetMapping("/verify-email/{token}")
    @Operation(summary = "Verify email and send temporary password")
    @ApiResponse(responseCode = "200", description = "Email verified, temporary password sent")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Parameter(description = "Email verification token")
            @PathVariable String token) {
        // Change this line:
        userService.verifyEmailAndSendCredentials(token);
        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully. Please check your email for login credentials."
        ));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid password")
    @ApiResponse(responseCode = "401", description = "Current password incorrect")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO,
            @Parameter(hidden = true) @RequestAttribute("userId") UUID userId) {
        userService.changePassword(userId, passwordChangeDTO);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve")
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping
    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user information")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, updateDTO));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Deactivate user")
    @ApiResponse(responseCode = "200", description = "User deactivated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }

    @PutMapping("/activate/{userId}")
    @Operation(summary = "Activate user")
    @ApiResponse(responseCode = "200", description = "User activated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> activateUser(
            @PathVariable UUID userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }

    @GetMapping("/department")
    @Operation(summary = "Get users by department")
    @ApiResponse(responseCode = "200", description = "Users found")
    @ApiResponse(responseCode = "404", description = "Department not found")
    public ResponseEntity<List<UserResponseDTO>> getUsersByDepartment(HttpServletRequest request) {
        String departmentId = request.getHeader("X-Department-Id");
        return ResponseEntity.ok(userService.getUsersByDepartment(UUID.fromString(departmentId)));
    }

    @GetMapping("/unverified")
    @Operation(summary = "Get all unverified users")
    @ApiResponse(responseCode = "200", description = "Unverified users retrieved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUnverifiedUsers() {
        return ResponseEntity.ok(userService.getUnverifiedUsers());
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive users")
    @ApiResponse(responseCode = "200", description = "Inactive users retrieved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getInactiveUsers(
            @Parameter(description = "Number of days of inactivity")
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(userService.getInactiveUsers(days));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user information")
    @ApiResponse(responseCode = "200", description = "Current user information retrieved")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @Parameter(hidden = true)
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user information")
    @ApiResponse(responseCode = "200", description = "Current user updated successfully")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @Parameter(hidden = true)
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, updateDTO));
    }

}