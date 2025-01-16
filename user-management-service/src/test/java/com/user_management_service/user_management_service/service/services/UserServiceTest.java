package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.*;
import com.user_management_service.user_management_service.repositories.*;
import com.user_management_service.user_management_service.services.AuditService;
import com.user_management_service.user_management_service.services.UserRoleClientService;
import com.user_management_service.user_management_service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import sopService.AssignRoleResponse;
import sopService.GetRoleByUserIdResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Mock
    private UserRoleClientService userRoleClientService;

    @Mock
    private KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UUID departmentId;
    private UUID roleId;
    private User testUser;
    private Department testDepartment;
    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        testDepartment = new Department();
        testDepartment.setId(departmentId);
        testDepartment.setName("Test Department");

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setDepartment(testDepartment);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setName("Test User");
        registrationDTO.setDepartmentId(departmentId);
        registrationDTO.setRoleId(roleId);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(departmentRepository.findById(any())).thenReturn(Optional.of(testDepartment));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);

        AssignRoleResponse roleResponse = AssignRoleResponse.newBuilder()
                .setSuccess(true)
                .build();
        when(userRoleClientService.assignRole(any(), any(), any())).thenReturn(roleResponse);

        GetRoleByUserIdResponse userRoleResponse = GetRoleByUserIdResponse.newBuilder()
                .setSuccess(true)
                .setRoleId(roleId.toString())
                .setRoleName("TEST_ROLE")
                .build();
        when(userRoleClientService.getUserRoles(any())).thenReturn(userRoleResponse);

        // Act
        UserResponseDTO result = userService.registerUser(registrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-created"), any(CustomUserDto.class));
        verify(auditService).logUserCreation(any(), any());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(any())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser(registrationDTO)
        );
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        GetRoleByUserIdResponse userRoleResponse = GetRoleByUserIdResponse.newBuilder()
                .setSuccess(true)
                .setRoleId(roleId.toString())
                .setRoleName("TEST_ROLE")
                .build();
        when(userRoleClientService.getUserRoles(any())).thenReturn(userRoleResponse);

        // Act
        UserResponseDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(UUID.randomUUID())
        );
    }

    @Test
    void updateUser_Success() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setName("Updated Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        GetRoleByUserIdResponse userRoleResponse = GetRoleByUserIdResponse.newBuilder()
                .setSuccess(true)
                .setRoleId(roleId.toString())
                .setRoleName("TEST_ROLE")
                .build();
        when(userRoleClientService.getUserRoles(any())).thenReturn(userRoleResponse);

        // Act
        UserResponseDTO result = userService.updateUser(userId, updateDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(auditService).logUserUpdate(any(), any());
    }

    @Test
    void verifyEmailAndSendCredentials_Success() {
        // Arrange
        String token = "valid-token";
        testUser.setEmailVerified(false);
        testUser.setEmailVerificationToken(token);
        testUser.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);

        // Act
        userService.verifyEmailAndSendCredentials(token);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("email-verified"), any(CustomUserDto.class));
        verify(auditService).logEmailVerification(any(), any());
    }

    @Test
    void changePassword_Success() {
        // Arrange
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setCurrentPassword("currentPass");
        dto.setNewPassword("newPass123!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        // Act
        userService.changePassword(userId, dto);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("password-updated"), any(CustomUserDto.class));
        verify(auditService).logPasswordChange(any(), any());
    }

    @Test
    void deactivateUser_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.deactivateUser(userId);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isActive());
        verify(kafkaTemplate).send(eq("user-deactivated"), any(CustomUserDto.class));
    }

    @Test
    void getUnverifiedUsers_Success() {
        // Arrange
        List<User> unverifiedUsers = Arrays.asList(testUser);
        when(userRepository.findByEmailVerifiedFalse()).thenReturn(unverifiedUsers);
        GetRoleByUserIdResponse userRoleResponse = GetRoleByUserIdResponse.newBuilder()
                .setSuccess(true)
                .setRoleId(roleId.toString())
                .setRoleName("TEST_ROLE")
                .build();
        when(userRoleClientService.getUserRoles(any())).thenReturn(userRoleResponse);

        // Act
        List<UserResponseDTO> result = userService.getUnverifiedUsers();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(unverifiedUsers.size(), result.size());
    }

    @Test
    void getInactiveUsers_Success() {
        // Arrange
        List<User> inactiveUsers = Arrays.asList(testUser);
        when(userRepository.findByLastLoginBeforeOrLastLoginIsNull(any()))
                .thenReturn(inactiveUsers);
        GetRoleByUserIdResponse userRoleResponse = GetRoleByUserIdResponse.newBuilder()
                .setSuccess(true)
                .setRoleId(roleId.toString())
                .setRoleName("TEST_ROLE")
                .build();
        when(userRoleClientService.getUserRoles(any())).thenReturn(userRoleResponse);

        // Act
        List<UserResponseDTO> result = userService.getInactiveUsers(30);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(inactiveUsers.size(), result.size());
    }

    @Test
    void getDepartmentUsersCount_Success() {
        // Arrange
        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(userRepository.countByDepartment(departmentId)).thenReturn(5L);

        // Act
        long count = userService.getDepartmentUsersCount(departmentId);

        // Assert
        assertEquals(5L, count);
        verify(userRepository).countByDepartment(departmentId);
    }
}