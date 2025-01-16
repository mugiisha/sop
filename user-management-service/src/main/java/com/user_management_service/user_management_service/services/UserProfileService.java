package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.enums.ErrorCode;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, CustomUserDto> kafkaTemplate;
    private final AuditService auditService;
    private final S3Service s3Service;

    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileDTO getUserProfile(UUID userId) {
        log.debug("Fetching user profile for ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", ErrorCode.DEPARTMENT_NOT_FOUND.getCode()));
        return mapToUserProfileDTO(user);
    }

    @Transactional
    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileDTO updateProfile(UUID userId, UserProfileUpdateDTO updateDTO) {
        log.info("Updating profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", ErrorCode.DEPARTMENT_NOT_FOUND.getCode()));

        if (updateDTO.getName() != null && !updateDTO.getName().isEmpty()) {
            user.setName(updateDTO.getName());
        }

        if (updateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }

        if (updateDTO.getCurrentPassword() != null && updateDTO.getNewPassword() != null) {
            updatePassword(userId, new PasswordUpdateDTO(updateDTO.getCurrentPassword(), updateDTO.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);
        auditService.logUserUpdate(user.getId(), user.getEmail());
        return mapToUserProfileDTO(updatedUser);
    }

    @Transactional
    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileDTO updateProfilePicture(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", ErrorCode.DEPARTMENT_NOT_FOUND.getCode()));

        try {
            if (user.getProfilePictureUrl() != null) {
                s3Service.deleteFile(user.getProfilePictureUrl());
            }

            String key = String.format("profiles/%s/%s-%s", userId, UUID.randomUUID(), file.getOriginalFilename());
            String fileUrl = s3Service.uploadFile(file, key);
            user.setProfilePictureUrl(fileUrl);

            User updatedUser = userRepository.save(user);
            auditService.logProfilePictureUpdate(user.getId(), user.getEmail());
            return mapToUserProfileDTO(updatedUser);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    @Transactional
    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileDTO removeProfilePicture(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", ErrorCode.DEPARTMENT_NOT_FOUND.getCode()));

        if (user.getProfilePictureUrl() != null) {
            s3Service.deleteFile(user.getProfilePictureUrl());
            user.setProfilePictureUrl(null);
            User updatedUser = userRepository.save(user);
            auditService.logProfilePictureUpdate(user.getId(), user.getEmail());
            return mapToUserProfileDTO(updatedUser);
        }
        return mapToUserProfileDTO(user);
    }

    @Transactional
    @CachePut(value = "userProfiles", key = "#userId")
    public void updatePassword(UUID userId, PasswordUpdateDTO passwordUpdateDTO) {
        log.info("Updating password for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", ErrorCode.DEPARTMENT_NOT_FOUND.getCode()));

        if (!passwordEncoder.matches(passwordUpdateDTO.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // prepare object to send via kafka
        CustomUserDto customUserDto = new CustomUserDto();
        customUserDto.setId(user.getId());
        customUserDto.setEmail(user.getEmail());
        customUserDto.setName(user.getName());

        kafkaTemplate.send("user-password-change", customUserDto);
        auditService.logPasswordChange(user.getId(), user.getEmail());
    }

    private UserProfileDTO mapToUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : null);
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}