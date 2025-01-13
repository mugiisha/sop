package com.sop_content_service.sop_content_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import userService.*;
import userService.userServiceGrpc.userServiceBlockingStub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserInfoClientServiceTest {

    @Mock
    private userServiceBlockingStub userInfoServiceBlockingStub;

    @InjectMocks
    private UserInfoClientService userInfoClientService;

    private static final String TEST_USER_ID = "user123";
    private static final String TEST_DEPARTMENT_ID = "dept456";
    private static final String TEST_USER_NAME = "John Doe";
    private static final String TEST_DEPARTMENT_NAME = "Engineering";
    private static final String TEST_PROFILE_URL = "http://example.com/profile.jpg";

    @BeforeEach
    void setUp() {
        // Method intentionally left empty as mocks are handled by annotations
    }

    @Test
    void getUserInfo_ShouldReturnValidResponse() {
        // Arrange
        getUserInfoResponse expectedResponse = getUserInfoResponse.newBuilder()
                .setSuccess(true)
                .setName(TEST_USER_NAME)
                .setProfilePictureUrl(TEST_PROFILE_URL)
                .setDepartmentName(TEST_DEPARTMENT_NAME)
                .build();

        getUserInfoRequest expectedRequest = getUserInfoRequest.newBuilder()
                .setUserId(TEST_USER_ID)
                .build();

        when(userInfoServiceBlockingStub.getUserInfo(any(getUserInfoRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        getUserInfoResponse actualResponse = userInfoClientService.getUserInfo(TEST_USER_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(TEST_USER_NAME, actualResponse.getName());
        assertEquals(TEST_PROFILE_URL, actualResponse.getProfilePictureUrl());
        assertEquals(TEST_DEPARTMENT_NAME, actualResponse.getDepartmentName());
    }

    @Test
    void getDepartmentName_ShouldReturnValidResponse() {
        // Arrange
        getDepartmentNameResponse expectedResponse = getDepartmentNameResponse.newBuilder()
                .setSuccess(true)
                .setDepartmentName(TEST_DEPARTMENT_NAME)
                .build();

        getDepartmentNameRequest expectedRequest = getDepartmentNameRequest.newBuilder()
                .setDepartmentId(TEST_DEPARTMENT_ID)
                .build();

        when(userInfoServiceBlockingStub.getDepartmentName(any(getDepartmentNameRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        getDepartmentNameResponse actualResponse = userInfoClientService.getDepartmentName(TEST_DEPARTMENT_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(TEST_DEPARTMENT_NAME, actualResponse.getDepartmentName());
    }

    @Test
    void getUserInfo_ShouldHandleErrorResponse() {
        // Arrange
        String errorMessage = "User not found";
        getUserInfoResponse errorResponse = getUserInfoResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(errorMessage)
                .build();

        when(userInfoServiceBlockingStub.getUserInfo(any(getUserInfoRequest.class)))
                .thenReturn(errorResponse);

        // Act
        getUserInfoResponse actualResponse = userInfoClientService.getUserInfo(TEST_USER_ID);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.getSuccess());
        assertEquals(errorMessage, actualResponse.getErrorMessage());
    }

    @Test
    void getDepartmentName_ShouldHandleErrorResponse() {
        // Arrange
        String errorMessage = "Department not found";
        getDepartmentNameResponse errorResponse = getDepartmentNameResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(errorMessage)
                .build();

        when(userInfoServiceBlockingStub.getDepartmentName(any(getDepartmentNameRequest.class)))
                .thenReturn(errorResponse);

        // Act
        getDepartmentNameResponse actualResponse = userInfoClientService.getDepartmentName(TEST_DEPARTMENT_ID);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.getSuccess());
        assertEquals(errorMessage, actualResponse.getErrorMessage());
    }
}