package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.enums.Role;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.*;
import com.user_management_service.user_management_service.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersByDepartment", allEntries = true),
            @CacheEvict(value = "unverifiedUsers", allEntries = true)
    })
    public UserResponseDTO createUser(UserRegistrationDTO registrationDTO) {
        log.info("Creating new user with email: {}", registrationDTO.getEmail());

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Department department = departmentRepository.findById(registrationDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        User user = createNewUser(registrationDTO, department);
        User savedUser = userRepository.save(user);

        createDefaultNotificationPreferences(savedUser);
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        auditService.logUserCreation(savedUser.getId(), savedUser.getEmail());

        return mapToUserResponseDTO(savedUser);
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponseDTO(user);
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

        Department department = null;
        if (updateDTO.getDepartmentId() != null && !updateDTO.getDepartmentId().equals(user.getDepartment().getId())) {
            department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            user.setDepartment(department);
        }

        if (updateDTO.getName() != null && !updateDTO.getName().isEmpty()) {
            user.setName(updateDTO.getName());
        }

        User updatedUser = userRepository.save(user);
        auditService.logUserUpdate(user.getId(), user.getEmail());

        return mapToUserResponseDTO(updatedUser);
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

        List<User> users = userRepository.findActiveUsersByDepartment(departmentId);
        return users.stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    @Cacheable(value = "unverifiedUsers")
    public List<UserResponseDTO> getUnverifiedUsers() {
        log.debug("Fetching all unverified users");
        List<User> unverifiedUsers = userRepository.findByEmailVerifiedFalse();
        return unverifiedUsers.stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    @Cacheable(value = "inactiveUsers")
    public List<UserResponseDTO> getInactiveUsers(int days) {
        log.debug("Fetching users inactive for {} days", days);
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<User> inactiveUsers = userRepository.findByLastLoginBeforeOrLastLoginIsNull(cutoffDate);
        return inactiveUsers.stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "unverifiedUsers", allEntries = true)
    public void resendVerificationEmail(UUID userId) {
        log.info("Resending verification email for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        String verificationToken = generateVerificationToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);
        auditService.logVerificationEmailResent(user.getId(), user.getEmail());
    }

    @Transactional
    @CacheEvict(value = "unverifiedUsers", allEntries = true)
    public void forceVerifyUser(UUID userId) {
        log.info("Force verifying user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("User is already verified");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        emailService.sendVerificationConfirmationEmail(user.getEmail(), user.getName());
        auditService.logForcedEmailVerification(user.getId(), user.getEmail());
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

    // Helper methods
    private User createNewUser(UserRegistrationDTO registrationDTO, Department department) {
        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setDepartment(department);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setEmailVerificationToken(generateVerificationToken());
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private void createDefaultNotificationPreferences(User user) {
        NotificationPreference preferences = new NotificationPreference();
        preferences.setUser(user);
        preferences.setEmailEnabled(true);
        preferences.setInAppEnabled(true);
        notificationPreferenceRepository.save(preferences);
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setDepartmentId(user.getDepartment().getId());
        dto.setDepartmentName(user.getDepartment().getName());
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRole(user.getRole());
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