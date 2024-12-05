package com.user_management_service.user_management_service.controllers;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.services.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        return ResponseEntity.ok(userProfileService.updateProfile(userId, updateDTO));
    }

    @PostMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDTO> uploadProfilePicture(
            @RequestAttribute("userId") UUID userId,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userProfileService.updateProfilePicture(userId, file));
    }

    @DeleteMapping("/me/picture")
    public ResponseEntity<UserProfileDTO> removeProfilePicture(
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userProfileService.removeProfilePicture(userId));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        userProfileService.updatePassword(userId, passwordUpdateDTO);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }


    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                        contentType.equals(MediaType.IMAGE_PNG_VALUE) ||
                        contentType.equals("image/jpg")
        );
    }
}
