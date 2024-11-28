package com.user_management_service.user_management_service.controllers;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "Operations for managing users including CRUD operations and user preferences")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Create a new user")
    @ApiResponse(responseCode = "200", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        return ResponseEntity.ok(userService.createUser(registrationDTO));
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
    @ApiResponse(responseCode = "200", description = "Users found")
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
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get users by department")
    @ApiResponse(responseCode = "200", description = "Users found")
    @ApiResponse(responseCode = "404", description = "Department not found")
    public ResponseEntity<List<UserResponseDTO>> getUsersByDepartment(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(userService.getUsersByDepartment(departmentId));
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

    @GetMapping("/{userId}/notification-preferences")
    @Operation(summary = "Get user notification preferences")
    @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<NotificationPreferenceDTO> getNotificationPreferences(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getNotificationPreferences(userId));
    }

    @PutMapping("/{userId}/notification-preferences")
    @Operation(summary = "Update user notification preferences")
    @ApiResponse(responseCode = "200", description = "Preferences updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<NotificationPreferenceDTO> updateNotificationPreferences(
            @PathVariable UUID userId,
            @Valid @RequestBody NotificationPreferenceDTO preferencesDTO) {
        return ResponseEntity.ok(userService.updateNotificationPreferences(userId, preferencesDTO));
    }

    @PostMapping("/{userId}/resend-verification")
    @Operation(summary = "Resend verification email")
    @ApiResponse(responseCode = "200", description = "Verification email sent successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @PathVariable UUID userId) {
        userService.resendVerificationEmail(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Verification email has been resent"
        ));
    }

    @PostMapping("/{userId}/force-verify")
    @Operation(summary = "Force verify user email")
    @ApiResponse(responseCode = "200", description = "User verified successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> forceVerifyUser(
            @PathVariable UUID userId) {
        userService.forceVerifyUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "User has been verified successfully"
        ));
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

    @GetMapping("/me/notification-preferences")
    @Operation(summary = "Get current user notification preferences")
    @ApiResponse(responseCode = "200", description = "Current user preferences retrieved")
    public ResponseEntity<NotificationPreferenceDTO> getCurrentUserNotificationPreferences(
            @Parameter(hidden = true)
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getNotificationPreferences(userId));
    }

    @PutMapping("/me/notification-preferences")
    @Operation(summary = "Update current user notification preferences")
    @ApiResponse(responseCode = "200", description = "Current user preferences updated")
    public ResponseEntity<NotificationPreferenceDTO> updateCurrentUserNotificationPreferences(
            @Parameter(hidden = true)
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody NotificationPreferenceDTO preferencesDTO) {
        return ResponseEntity.ok(userService.updateNotificationPreferences(userId, preferencesDTO));
    }
}