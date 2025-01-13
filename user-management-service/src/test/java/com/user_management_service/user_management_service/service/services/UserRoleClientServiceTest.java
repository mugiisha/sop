package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.services.UserRoleClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopService.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserRoleClientServiceTest {

    @Mock
    private RoleAssignmentServiceGrpc.RoleAssignmentServiceBlockingStub roleAssignmentServiceBlockingStub;

    @InjectMocks
    private UserRoleClientService userRoleClientService;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_ROLE_ID = "test-role-456";
    private static final String TEST_DEPARTMENT_ID = "test-dept-789";
    private static final String TEST_ROLE_NAME = "TEST_ROLE";

    @Test
    void assignRole_Success() {
        // Arrange
        AssignRoleRequest expectedRequest = AssignRoleRequest.newBuilder()
                .setUserId(TEST_USER_ID)
                .setRoleId(TEST_ROLE_ID)
                .setDepartmentId(TEST_DEPARTMENT_ID)
                .build();

        AssignRoleResponse expectedResponse = AssignRoleResponse.newBuilder()
                .setSuccess(true)
                .build();

        when(roleAssignmentServiceBlockingStub.assignRole(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        AssignRoleResponse actualResponse = userRoleClientService.assignRole(
                TEST_USER_ID, TEST_ROLE_ID, TEST_DEPARTMENT_ID);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(roleAssignmentServiceBlockingStub).assignRole(expectedRequest);
    }

    @Test
    void getUserRoles_Success() {
        // Arrange
        GetRoleByUserIdRequest expectedRequest = GetRoleByUserIdRequest.newBuilder()
                .setUserId(TEST_USER_ID)
                .build();

        GetRoleByUserIdResponse expectedResponse = GetRoleByUserIdResponse.newBuilder()
                .setSuccess(true)
                .build();

        when(roleAssignmentServiceBlockingStub.getRoleAssignmentsByUserId(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        GetRoleByUserIdResponse actualResponse = userRoleClientService.getUserRoles(TEST_USER_ID);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(roleAssignmentServiceBlockingStub).getRoleAssignmentsByUserId(expectedRequest);
    }

    @Test
    void getUsersByRoleIdAndDepartmentId_Success() {
        // Arrange
        GetRoleAssignmentsByRoleIdAndDepartmentIdRequest expectedRequest =
                GetRoleAssignmentsByRoleIdAndDepartmentIdRequest.newBuilder()
                        .setRoleId(TEST_ROLE_ID)
                        .setDepartmentId(TEST_DEPARTMENT_ID)
                        .build();

        GetRoleAssignmentsByRoleIdAndDepartmentIdResponse expectedResponse =
                GetRoleAssignmentsByRoleIdAndDepartmentIdResponse.newBuilder()
                        .setSuccess(true)
                        .build();

        when(roleAssignmentServiceBlockingStub.getRoleAssignmentsByRoleIdAndDepartmentId(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        GetRoleAssignmentsByRoleIdAndDepartmentIdResponse actualResponse =
                userRoleClientService.getUsersByRoleIdAndDepartmentId(TEST_ROLE_ID, TEST_DEPARTMENT_ID);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(roleAssignmentServiceBlockingStub)
                .getRoleAssignmentsByRoleIdAndDepartmentId(expectedRequest);
    }

    @Test
    void updateRoleAssignment_Success() {
        // Arrange
        AssignRoleRequest expectedRequest = AssignRoleRequest.newBuilder()
                .setUserId(TEST_USER_ID)
                .setRoleId(TEST_ROLE_ID)
                .setDepartmentId(TEST_DEPARTMENT_ID)
                .build();

        AssignRoleResponse expectedResponse = AssignRoleResponse.newBuilder()
                .setSuccess(true)
                .build();

        when(roleAssignmentServiceBlockingStub.updateUserRole(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        AssignRoleResponse actualResponse = userRoleClientService
                .updateRoleAssignment(TEST_USER_ID, TEST_ROLE_ID, TEST_DEPARTMENT_ID);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(roleAssignmentServiceBlockingStub).updateUserRole(expectedRequest);
    }

    @Test
    void deleteUserRole_Success() {
        // Arrange
        DeleteUserRoleRequest expectedRequest = DeleteUserRoleRequest.newBuilder()
                .setUserId(TEST_USER_ID)
                .build();

        DeleteUserRoleResponse expectedResponse = DeleteUserRoleResponse.newBuilder()
                .setSuccess(true)
                .build();

        when(roleAssignmentServiceBlockingStub.deleteUserRole(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        DeleteUserRoleResponse actualResponse = userRoleClientService.deleteUserRole(TEST_USER_ID);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(roleAssignmentServiceBlockingStub).deleteUserRole(expectedRequest);
    }

    @Test
    void getRoleByName_Success() {
        // Arrange
        GetRoleByNameRequest expectedRequest = GetRoleByNameRequest.newBuilder()
                .setRoleName(TEST_ROLE_NAME)
                .build();

        GetRoleByNameResponse expectedResponse = GetRoleByNameResponse.newBuilder()
                .setSuccess(true)
                .build();

        when(roleAssignmentServiceBlockingStub.getRoleByName(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        GetRoleByNameResponse actualResponse = userRoleClientService.getRoleByName(TEST_ROLE_NAME);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(roleAssignmentServiceBlockingStub).getRoleByName(expectedRequest);
    }

    @Test
    void assignRole_Failure() {
        // Arrange
        AssignRoleRequest expectedRequest = AssignRoleRequest.newBuilder()
                .setUserId(TEST_USER_ID)
                .setRoleId(TEST_ROLE_ID)
                .setDepartmentId(TEST_DEPARTMENT_ID)
                .build();

        AssignRoleResponse expectedResponse = AssignRoleResponse.newBuilder()
                .setSuccess(false)
                .build();

        when(roleAssignmentServiceBlockingStub.assignRole(expectedRequest))
                .thenReturn(expectedResponse);

        // Act
        AssignRoleResponse actualResponse = userRoleClientService
                .assignRole(TEST_USER_ID, TEST_ROLE_ID, TEST_DEPARTMENT_ID);

        // Assert
        assertFalse(actualResponse.getSuccess());
        verify(roleAssignmentServiceBlockingStub).assignRole(expectedRequest);
    }
}