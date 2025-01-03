package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.*;
import com.user_management_service.user_management_service.repositories.*;
import com.user_management_service.user_management_service.utils.PasswordGenerator;
import com.user_management_service.user_management_service.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopService.AssignRoleResponse;
import sopService.GetRoleByUserIdResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class  UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final UserRoleClientService userRoleClientService;
    private final KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering new user with email: {}", registrationDTO.getEmail());

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Department department = departmentRepository.findById(registrationDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        String temporaryPassword = PasswordGenerator.generateRandomPassword();
        String verificationToken = UUID.randomUUID().toString();

        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setDepartment(department);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setMustChangePassword(true);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        AssignRoleResponse response = userRoleClientService.assignRole(
                savedUser.getId().toString(),
                registrationDTO.getRoleId().toString(),
                registrationDTO.getDepartmentId().toString()
        );

        if (!response.getSuccess()) {
            userRepository.delete(savedUser);
            throw new RoleServerException(response.getErrorMessage());
        }

        GetRoleByUserIdResponse userRole = userRoleClientService.getUserRoles(savedUser.getId().toString());
        if (!userRole.getSuccess()) {
            throw new RoleServerException(userRole.getErrorMessage());
        }

        CustomUserDto createdUserDto = new CustomUserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                null,
                user.getEmailVerificationToken()
        );

        kafkaTemplate.send("user-created", createdUserDto);

        auditService.logUserCreation(savedUser.getId(), savedUser.getEmail());

        return mapToUserResponseDTO(savedUser, userRole);
    }

    @Cacheable(value = "user", key = "#id")
    public UserResponseDTO getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"+id));

        GetRoleByUserIdResponse userRole = userRoleClientService
                .getUserRoles(user.getId().toString());

        if (!userRole.getSuccess()) {
            throw new RoleServerException(userRole.getErrorMessage());
        }

        return mapToUserResponseDTO(user, userRole);
    }

    @Transactional
    @CachePut(value = "user", key = "#id")
    public UserResponseDTO updateUser(UUID id, UserUpdateDTO updateDTO) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updateDTO.getDepartmentId() != null &&
                !updateDTO.getDepartmentId().equals(user.getDepartment().getId())) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            user.setDepartment(department);
        }

        if (updateDTO.getName() != null && !updateDTO.getName().isEmpty()) {
            user.setName(updateDTO.getName());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        GetRoleByUserIdResponse userRole = userRoleClientService
                .getUserRoles(updatedUser.getId().toString());

        if (!userRole.getSuccess()) {
            throw new RoleServerException(userRole.getErrorMessage());
        }

        auditService.logUserUpdate(user.getId(), user.getEmail());

        return mapToUserResponseDTO(updatedUser, userRole);
    }

    @Transactional
    public void verifyEmailAndSendCredentials(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        String temporaryPassword = PasswordGenerator.generateRandomPassword();

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        CustomUserDto createdUserDto = new CustomUserDto();
        createdUserDto.setId(user.getId());
        createdUserDto.setEmail(user.getEmail());
        createdUserDto.setName(user.getName());
        createdUserDto.setPassword(temporaryPassword);

        kafkaTemplate.send("email-verified", createdUserDto);

        auditService.logEmailVerification(user.getId(), user.getEmail());
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public void changePassword(UUID userId, PasswordChangeDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        PasswordValidator.validatePassword(dto.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        CustomUserDto createdUserDto = new CustomUserDto();
        createdUserDto.setId(user.getId());
        createdUserDto.setEmail(user.getEmail());
        createdUserDto.setName(user.getName());

        kafkaTemplate.send("password-updated", createdUserDto);
        auditService.logPasswordChange(user.getId(), user.getEmail());
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("User is already deactivated");
        }

        user.setActive(false);
        user.setDeactivatedAt(LocalDateTime.now());
        userRepository.save(user);

        CustomUserDto customUserDto = new CustomUserDto();
        customUserDto.setId(user.getId());
        customUserDto.setEmail(user.getEmail());
        customUserDto.setName(user.getName());

        kafkaTemplate.send("user-deactivated", customUserDto);
        auditService.logUserDeactivation(user.getId(), user.getEmail());
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public void activateUser(UUID id) {
        log.info("activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isActive()) {
            throw new IllegalStateException("User is already active");
        }

        user.setActive(true);
        user.setDeactivatedAt(null);
        userRepository.save(user);

        CustomUserDto customUserDto = new CustomUserDto();
        customUserDto.setId(user.getId());
        customUserDto.setEmail(user.getEmail());
        customUserDto.setName(user.getName());

        kafkaTemplate.send("user-activated", customUserDto);
        auditService.logUserDeactivation(user.getId(), user.getEmail());
    }

    public List<UserResponseDTO> getUsersByDepartment(UUID departmentId) {
        log.debug("Fetching users for department ID: {}", departmentId);

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found");
        }

        return userRepository.findActiveUsersByDepartment(departmentId).stream()
                .map(user -> {
                    GetRoleByUserIdResponse userRole = userRoleClientService
                            .getUserRoles(user.getId().toString());
                    if (!userRole.getSuccess()) {
                        throw new RoleServerException(userRole.getErrorMessage());
                    }
                    return mapToUserResponseDTO(user, userRole);
                })
                .toList();
    }

    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    GetRoleByUserIdResponse userRole = userRoleClientService
                            .getUserRoles(user.getId().toString());
                    if (!userRole.getSuccess()) {
                        throw new RoleServerException(userRole.getErrorMessage());
                    }
                    return mapToUserResponseDTO(user, userRole);
                })
                .toList();
    }

    public List<UserResponseDTO> getUnverifiedUsers() {
        log.debug("Fetching all unverified users");
        return userRepository.findByEmailVerifiedFalse().stream()
                .map(user -> {
                    GetRoleByUserIdResponse userRole = userRoleClientService
                            .getUserRoles(user.getId().toString());
                    if (!userRole.getSuccess()) {
                        throw new RoleServerException(userRole.getErrorMessage());
                    }
                    return mapToUserResponseDTO(user, userRole);
                })
                .toList();
    }

    public List<UserResponseDTO> getInactiveUsers(int days) {
        log.debug("Fetching users inactive for {} days", days);

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRepository.findByLastLoginBeforeOrLastLoginIsNull(cutoffDate).stream()
                .map(user -> {
                    GetRoleByUserIdResponse userRole = userRoleClientService
                            .getUserRoles(user.getId().toString());
                    if (!userRole.getSuccess()) {
                        throw new RoleServerException(userRole.getErrorMessage());
                    }
                    return mapToUserResponseDTO(user, userRole);
                })
                .toList();
    }


    private UserResponseDTO mapToUserResponseDTO(User user, GetRoleByUserIdResponse userRole) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setDepartmentId(user.getDepartment().getId());
        dto.setDepartmentName(user.getDepartment().getName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRoleId(userRole.getRoleId());
        dto.setRoleName(userRole.getRoleName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}