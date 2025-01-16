package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.dtos.DepartmentDTO;
import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.services.DepartmentService;
import com.user_management_service.user_management_service.services.UserInfoService;
import com.user_management_service.user_management_service.services.UserService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import userService.getDepartmentNameRequest;
import userService.getDepartmentNameResponse;
import userService.getUserInfoRequest;
import userService.getUserInfoResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private StreamObserver<getUserInfoResponse> userInfoResponseObserver;

    @Mock
    private StreamObserver<getDepartmentNameResponse> departmentNameResponseObserver;

    @InjectMocks
    private UserInfoService userInfoService;

    @Captor
    private ArgumentCaptor<getUserInfoResponse> userInfoResponseCaptor;

    @Captor
    private ArgumentCaptor<getDepartmentNameResponse> departmentNameResponseCaptor;

    private UUID userId;
    private UUID departmentId;
    private UserResponseDTO mockUserResponse;
    private DepartmentDTO mockDepartmentResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        mockUserResponse = new UserResponseDTO();
        mockUserResponse.setName("Test User");
        mockUserResponse.setProfilePictureUrl("http://example.com/picture.jpg");
        mockUserResponse.setDepartmentName("IT Department");

        mockDepartmentResponse = new DepartmentDTO();
        mockDepartmentResponse.setName("IT Department");
    }

    @Test
    void getUserInfo_WhenSuccessful_ShouldReturnUserInfo() {
        // Arrange
        getUserInfoRequest request = getUserInfoRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        when(userService.getUserById(userId)).thenReturn(mockUserResponse);

        // Act
        userInfoService.getUserInfo(request, userInfoResponseObserver);

        // Assert
        verify(userInfoResponseObserver).onNext(userInfoResponseCaptor.capture());
        verify(userInfoResponseObserver).onCompleted();

        getUserInfoResponse capturedResponse = userInfoResponseCaptor.getValue();
        assertTrue(capturedResponse.getSuccess());
        assertEquals(mockUserResponse.getName(), capturedResponse.getName());
        assertEquals(mockUserResponse.getProfilePictureUrl(), capturedResponse.getProfilePictureUrl());
        assertEquals(mockUserResponse.getDepartmentName(), capturedResponse.getDepartmentName());
    }

    @Test
    void getUserInfo_WhenUserServiceThrowsException_ShouldReturnError() {
        // Arrange
        getUserInfoRequest request = getUserInfoRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        when(userService.getUserById(any())).thenThrow(new RuntimeException("User not found"));

        // Act
        userInfoService.getUserInfo(request, userInfoResponseObserver);

        // Assert
        verify(userInfoResponseObserver).onNext(userInfoResponseCaptor.capture());
        verify(userInfoResponseObserver).onCompleted();

        getUserInfoResponse capturedResponse = userInfoResponseCaptor.getValue();
        assertFalse(capturedResponse.getSuccess());
        assertTrue(capturedResponse.getErrorMessage().contains("Failed to get user info"));
    }

    @Test
    void getUserInfo_WhenInvalidUUID_ShouldReturnError() {
        // Arrange
        getUserInfoRequest request = getUserInfoRequest.newBuilder()
                .setUserId("invalid-uuid")
                .build();

        // Act
        userInfoService.getUserInfo(request, userInfoResponseObserver);

        // Assert
        verify(userInfoResponseObserver).onNext(userInfoResponseCaptor.capture());
        verify(userInfoResponseObserver).onCompleted();

        getUserInfoResponse capturedResponse = userInfoResponseCaptor.getValue();
        assertFalse(capturedResponse.getSuccess());
        assertTrue(capturedResponse.getErrorMessage().contains("Failed to get user info"));
    }

    @Test
    void getDepartmentName_WhenSuccessful_ShouldReturnDepartmentName() {
        // Arrange
        getDepartmentNameRequest request = getDepartmentNameRequest.newBuilder()
                .setDepartmentId(departmentId.toString())
                .build();

        when(departmentService.getDepartmentById(departmentId)).thenReturn(mockDepartmentResponse);

        // Act
        userInfoService.getDepartmentName(request, departmentNameResponseObserver);

        // Assert
        verify(departmentNameResponseObserver).onNext(departmentNameResponseCaptor.capture());
        verify(departmentNameResponseObserver).onCompleted();

        getDepartmentNameResponse capturedResponse = departmentNameResponseCaptor.getValue();
        assertTrue(capturedResponse.getSuccess());
        assertEquals(mockDepartmentResponse.getName(), capturedResponse.getDepartmentName());
    }

    @Test
    void getDepartmentName_WhenDepartmentServiceThrowsException_ShouldReturnError() {
        // Arrange
        getDepartmentNameRequest request = getDepartmentNameRequest.newBuilder()
                .setDepartmentId(departmentId.toString())
                .build();

        when(departmentService.getDepartmentById(any())).thenThrow(new RuntimeException("Department not found"));

        // Act
        userInfoService.getDepartmentName(request, departmentNameResponseObserver);

        // Assert
        verify(departmentNameResponseObserver).onNext(departmentNameResponseCaptor.capture());
        verify(departmentNameResponseObserver).onCompleted();

        getDepartmentNameResponse capturedResponse = departmentNameResponseCaptor.getValue();
        assertFalse(capturedResponse.getSuccess());
        assertTrue(capturedResponse.getErrorMessage().contains("Failed to fetch department name"));
    }

    @Test
    void getDepartmentName_WhenInvalidUUID_ShouldReturnError() {
        // Arrange
        getDepartmentNameRequest request = getDepartmentNameRequest.newBuilder()
                .setDepartmentId("invalid-uuid")
                .build();

        // Act
        userInfoService.getDepartmentName(request, departmentNameResponseObserver);

        // Assert
        verify(departmentNameResponseObserver).onNext(departmentNameResponseCaptor.capture());
        verify(departmentNameResponseObserver).onCompleted();

        getDepartmentNameResponse capturedResponse = departmentNameResponseCaptor.getValue();
        assertFalse(capturedResponse.getSuccess());
        assertTrue(capturedResponse.getErrorMessage().contains("Failed to fetch department name"));
    }
}