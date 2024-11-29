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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;
    private final UserRoleClientService userRoleClientService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersByDepartment", allEntries = true),
            @CacheEvict(value = "unverifiedUsers", allEntries = true)
    })
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

        createDefaultNotificationPreferences(savedUser);

        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getName(),
                verificationToken,
                temporaryPassword
        );

        auditService.logUserCreation(savedUser.getId(), savedUser.getEmail());

        return mapToUserResponseDTO(savedUser, "");
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GetRoleByUserIdResponse userRole = userRoleClientService
                .getUserRoles(user.getId().toString());

        if (!userRole.getSuccess()) {
            throw new RoleServerException(userRole.getErrorMessage());
        }

        return mapToUserResponseDTO(user, userRole.getRoleName());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "usersByDepartment", allEntries = true)
    })
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
        auditService.logUserUpdate(user.getId(), user.getEmail());

        return mapToUserResponseDTO(updatedUser, "");
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
        emailService.sendVerificationConfirmationEmail(user.getEmail(), user.getName(), temporaryPassword);
        auditService.logEmailVerification(user.getId(), user.getEmail());
    }

    @Transactional
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
        emailService.sendPasswordChangeConfirmation(user.getEmail(), user.getName());
        auditService.logPasswordChange(user.getId(), user.getEmail());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "usersByDepartment", allEntries = true),
            @CacheEvict(value = "inactiveUsers", allEntries = true)
    })
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

        emailService.sendAccountDeactivationEmail(user.getEmail(), user.getName());
        auditService.logUserDeactivation(user.getId(), user.getEmail());
    }

    @Cacheable(value = "usersByDepartment")
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
                    return mapToUserResponseDTO(user, userRole.getRoleName());
                })
                .toList();
    }

    @Cacheable(value = "users")
    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    GetRoleByUserIdResponse userRole = userRoleClientService
                            .getUserRoles(user.getId().toString());
                    if (!userRole.getSuccess()) {
                        throw new RoleServerException(userRole.getErrorMessage());
                    }
                    return mapToUserResponseDTO(user, userRole.getRoleName());
                })
                .toList();
    }

    @Cacheable(value = "unverifiedUsers")
    public List<UserResponseDTO> getUnverifiedUsers() {
        log.debug("Fetching all unverified users");
        return userRepository.findByEmailVerifiedFalse().stream()
                .map(user -> mapToUserResponseDTO(user, ""))
                .toList();
    }

    @Cacheable(value = "inactiveUsers")
    public List<UserResponseDTO> getInactiveUsers(int days) {
        log.debug("Fetching users inactive for {} days", days);

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRepository.findByLastLoginBeforeOrLastLoginIsNull(cutoffDate).stream()
                .map(user -> mapToUserResponseDTO(user, ""))
                .toList();
    }

    @Cacheable(value = "notificationPreferences", key = "#userId")
    public NotificationPreferenceDTO getNotificationPreferences(UUID userId) {
        log.debug("Fetching notification preferences for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NotificationPreference preferences = notificationPreferenceRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification preferences not found"));

        return mapToNotificationPreferenceDTO(preferences);
    }

    @Transactional
    @CacheEvict(value = "notificationPreferences", key = "#userId")
    public NotificationPreferenceDTO updateNotificationPreferences(
            UUID userId,
            NotificationPreferenceDTO preferencesDTO) {
        log.info("Updating notification preferences for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NotificationPreference preferences = notificationPreferenceRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference newPref = new NotificationPreference();
                    newPref.setUser(user);
                    return newPref;
                });

        preferences.setEmailEnabled(preferencesDTO.isEmailEnabled());
        preferences.setInAppEnabled(preferencesDTO.isInAppEnabled());

        NotificationPreference savedPreferences = notificationPreferenceRepository.save(preferences);
        auditService.logNotificationPreferenceUpdate(user.getId(), user.getEmail());

        return mapToNotificationPreferenceDTO(savedPreferences);
    }

    private void createDefaultNotificationPreferences(User user) {
        NotificationPreference preferences = new NotificationPreference();
        preferences.setUser(user);
        preferences.setEmailEnabled(true);
        preferences.setInAppEnabled(true);
        notificationPreferenceRepository.save(preferences);
    }

    private UserResponseDTO mapToUserResponseDTO(User user, String roleName) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setDepartmentId(user.getDepartment().getId());
        dto.setDepartmentName(user.getDepartment().getName());
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRoleName(roleName);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private NotificationPreferenceDTO mapToNotificationPreferenceDTO(NotificationPreference preferences) {
        NotificationPreferenceDTO dto = new NotificationPreferenceDTO();
        dto.setUserId(preferences.getUser().getId());
        dto.setEmailEnabled(preferences.isEmailEnabled());
        dto.setInAppEnabled(preferences.isInAppEnabled());
        return dto;
    }
}