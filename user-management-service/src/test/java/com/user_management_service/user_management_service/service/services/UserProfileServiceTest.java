package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.exceptions.*;
import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.repositories.UserRepository;
import com.user_management_service.user_management_service.services.AuditService;
import com.user_management_service.user_management_service.services.S3Service;
import com.user_management_service.user_management_service.services.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, CustomUserDto> kafkaTemplate;

    @Mock
    private AuditService auditService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserProfileService userProfileService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<CustomUserDto> customUserDtoCaptor;

    private User testUser;
    private UUID userId;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("IT Department");

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedPassword");
        testUser.setPhoneNumber("+1234567890");
        testUser.setProfilePictureUrl("http://example.com/picture.jpg");
        testUser.setDepartment(testDepartment);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setLastLogin(LocalDateTime.now());
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getUserProfile_WhenUserExists_ShouldReturnUserProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserProfileDTO result = userProfileService.getUserProfile(userId);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(testUser.getProfilePictureUrl(), result.getProfilePictureUrl());
        assertEquals(testUser.getDepartment().getName(), result.getDepartmentName());
    }

    @Test
    void getUserProfile_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userProfileService.getUserProfile(userId));
    }

    @Test
    void updateProfile_WhenValidUpdate_ShouldUpdateUserProfile() {
        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setPhoneNumber("+9876543210");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileDTO result = userProfileService.updateProfile(userId, updateDTO);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("Updated Name", savedUser.getName());
        assertEquals("+9876543210", savedUser.getPhoneNumber());
        verify(auditService).logUserUpdate(userId, testUser.getEmail());
    }

    @Test
    void updateProfilePicture_WhenValidFile_ShouldUpdateProfilePicture() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String expectedUrl = "http://example.com/new-picture.jpg";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(s3Service.uploadFile(any(MultipartFile.class), anyString())).thenReturn(expectedUrl);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileDTO result = userProfileService.updateProfilePicture(userId, file);

        verify(s3Service).deleteFile(testUser.getProfilePictureUrl());
        verify(s3Service).uploadFile(eq(file), anyString());
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(expectedUrl, savedUser.getProfilePictureUrl());
        verify(auditService).logProfilePictureUpdate(userId, testUser.getEmail());
    }

    @Test
    void removeProfilePicture_WhenPictureExists_ShouldRemoveProfilePicture() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileDTO result = userProfileService.removeProfilePicture(userId);

        verify(s3Service).deleteFile(testUser.getProfilePictureUrl());
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNull(savedUser.getProfilePictureUrl());
        verify(auditService).logProfilePictureUpdate(userId, testUser.getEmail());
    }

    @Test
    void updatePassword_WhenValidPasswordUpdate_ShouldUpdatePassword() {
        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO("currentPassword", "newPassword");
        String newHashedPassword = "newHashedPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(passwordUpdateDTO.getCurrentPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(passwordUpdateDTO.getNewPassword())).thenReturn(newHashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        userProfileService.updatePassword(userId, passwordUpdateDTO);

        verify(userRepository).save(userCaptor.capture());
        verify(kafkaTemplate).send(eq("user-password-change"), customUserDtoCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(newHashedPassword, savedUser.getPasswordHash());
        assertFalse(savedUser.isMustChangePassword());

        CustomUserDto sentDto = customUserDtoCaptor.getValue();
        assertEquals(userId, sentDto.getId());
        assertEquals(testUser.getEmail(), sentDto.getEmail());
        assertEquals(testUser.getName(), sentDto.getName());

        verify(auditService).logPasswordChange(userId, testUser.getEmail());
    }

    @Test
    void updatePassword_WhenCurrentPasswordIncorrect_ShouldThrowInvalidPasswordException() {
        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO("wrongPassword", "newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(passwordUpdateDTO.getCurrentPassword(), testUser.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () ->
                userProfileService.updatePassword(userId, passwordUpdateDTO));

        verify(userRepository, never()).save(any(User.class));
        verify(kafkaTemplate, never()).send(anyString(), any(CustomUserDto.class));
    }

    @Test
    void updateProfile_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
        updateDTO.setName("Updated Name");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userProfileService.updateProfile(userId, updateDTO));
    }

    @Test
    void updateProfilePicture_WhenS3UploadFails_ShouldThrowRuntimeException() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(s3Service.uploadFile(any(MultipartFile.class), anyString())).thenThrow(new IOException("Upload failed"));

        assertThrows(RuntimeException.class, () ->
                userProfileService.updateProfilePicture(userId, file));

        verify(userRepository, never()).save(any(User.class));
    }
}