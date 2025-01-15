package com.role_access_control_service.role_access_control_service;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import com.role_access_control_service.role_access_control_service.services.RoleAssignmentService;
import com.role_access_control_service.role_access_control_service.services.RoleService;
import com.role_access_control_service.role_access_control_service.services.UserRoleService;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopService.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserRoleService userRoleService;

    @Mock
    private StreamObserver<GetRoleByUserIdResponse> roleByUserIdResponseObserver;

    @Mock
    private StreamObserver<AssignRoleResponse> assignRoleResponseObserver;

    @Mock
    private StreamObserver<GetRoleAssignmentsByRoleIdAndDepartmentIdResponse> roleByDeptResponseObserver;

    @Mock
    private StreamObserver<DeleteUserRoleResponse> deleteRoleResponseObserver;

    @Mock
    private StreamObserver<GetRoleByNameResponse> roleByNameResponseObserver;

    @Captor
    private ArgumentCaptor<GetRoleByUserIdResponse> roleByUserIdResponseCaptor;

    @Captor
    private ArgumentCaptor<AssignRoleResponse> assignRoleResponseCaptor;

    @Captor
    private ArgumentCaptor<GetRoleAssignmentsByRoleIdAndDepartmentIdResponse> roleByDeptResponseCaptor;

    @Captor
    private ArgumentCaptor<DeleteUserRoleResponse> deleteRoleResponseCaptor;

    @Captor
    private ArgumentCaptor<GetRoleByNameResponse> roleByNameResponseCaptor;

    private UUID userId;
    private UUID roleId;
    private UUID departmentId;
    private UUID assignmentId;
    private Role role;
    private RoleAssignment roleAssignment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();

        role = Role.builder()
                .id(roleId)
                .roleName("STAFF")
                .createdAt(LocalDateTime.now())
                .build();

        roleAssignment = RoleAssignment.builder()
                .id(assignmentId)
                .userId(userId)
                .role(role)
                .departmentId(departmentId)
                .assignedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getRoleAssignmentsByUserId_Success() {
        // Arrange
        GetRoleByUserIdRequest request = GetRoleByUserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        when(roleAssignmentService.getUserRoles(userId)).thenReturn(roleAssignment);

        // Act
        userRoleService.getRoleAssignmentsByUserId(request, roleByUserIdResponseObserver);

        // Assert
        verify(roleByUserIdResponseObserver).onNext(roleByUserIdResponseCaptor.capture());
        verify(roleByUserIdResponseObserver).onCompleted();

        GetRoleByUserIdResponse response = roleByUserIdResponseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(role.getRoleName(), response.getRoleName());
        assertEquals(roleId.toString(), response.getRoleId());
    }

    @Test
    void getRoleAssignmentsByUserId_Failure() {
        // Arrange
        GetRoleByUserIdRequest request = GetRoleByUserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        when(roleAssignmentService.getUserRoles(userId))
                .thenThrow(new NotFoundException("Role assignment not found"));

        // Act
        userRoleService.getRoleAssignmentsByUserId(request, roleByUserIdResponseObserver);

        // Assert
        verify(roleByUserIdResponseObserver).onNext(roleByUserIdResponseCaptor.capture());
        verify(roleByUserIdResponseObserver).onCompleted();

        GetRoleByUserIdResponse response = roleByUserIdResponseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getErrorMessage().contains("Role assignment not found"));
    }

    @Test
    void assignRole_Success() {
        // Arrange
        AssignRoleRequest request = AssignRoleRequest.newBuilder()
                .setUserId(userId.toString())
                .setRoleId(roleId.toString())
                .setDepartmentId(departmentId.toString())
                .build();

        when(roleAssignmentService.assignRole(any(AssignRoleDto.class))).thenReturn(roleAssignment);

        // Act
        userRoleService.assignRole(request, assignRoleResponseObserver);

        // Assert
        verify(assignRoleResponseObserver).onNext(assignRoleResponseCaptor.capture());
        verify(assignRoleResponseObserver).onCompleted();

        AssignRoleResponse response = assignRoleResponseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(assignmentId.toString(), response.getAssignmentId());
    }

    @Test
    void assignRole_Failure() {
        // Arrange
        AssignRoleRequest request = AssignRoleRequest.newBuilder()
                .setUserId(userId.toString())
                .setRoleId(roleId.toString())
                .setDepartmentId(departmentId.toString())
                .build();

        when(roleAssignmentService.assignRole(any(AssignRoleDto.class)))
                .thenThrow(new RuntimeException("Failed to assign role"));

        // Act
        userRoleService.assignRole(request, assignRoleResponseObserver);

        // Assert
        verify(assignRoleResponseObserver).onNext(assignRoleResponseCaptor.capture());
        verify(assignRoleResponseObserver).onCompleted();

        AssignRoleResponse response = assignRoleResponseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getErrorMessage().contains("Failed to assign role"));
    }

    @Test
    void getRoleAssignmentsByRoleIdAndDepartmentId_Success() {
        // Arrange
        GetRoleAssignmentsByRoleIdAndDepartmentIdRequest request = GetRoleAssignmentsByRoleIdAndDepartmentIdRequest.newBuilder()
                .setRoleId(roleId.toString())
                .setDepartmentId(departmentId.toString())
                .build();

        List<RoleAssignment> assignments = Arrays.asList(roleAssignment);
        when(roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(roleId, departmentId))
                .thenReturn(assignments);

        // Act
        userRoleService.getRoleAssignmentsByRoleIdAndDepartmentId(request, roleByDeptResponseObserver);

        // Assert
        verify(roleByDeptResponseObserver).onNext(roleByDeptResponseCaptor.capture());
        verify(roleByDeptResponseObserver).onCompleted();

        GetRoleAssignmentsByRoleIdAndDepartmentIdResponse response = roleByDeptResponseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(1, response.getUserIdsCount());
        assertEquals(userId.toString(), response.getUserIds(0));
    }

    @Test
    void updateUserRole_Success() {
        // Arrange
        AssignRoleRequest request = AssignRoleRequest.newBuilder()
                .setUserId(userId.toString())
                .setRoleId(roleId.toString())
                .setDepartmentId(departmentId.toString())
                .build();

        when(roleAssignmentService.updateRoleAssignment(any(AssignRoleDto.class))).thenReturn(roleAssignment);

        // Act
        userRoleService.updateUserRole(request, assignRoleResponseObserver);

        // Assert
        verify(assignRoleResponseObserver).onNext(assignRoleResponseCaptor.capture());
        verify(assignRoleResponseObserver).onCompleted();

        AssignRoleResponse response = assignRoleResponseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(assignmentId.toString(), response.getAssignmentId());
    }

    @Test
    void deleteUserRole_Success() {
        // Arrange
        DeleteUserRoleRequest request = DeleteUserRoleRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        doNothing().when(roleAssignmentService).deleteRoleAssignment(userId);

        // Act
        userRoleService.deleteUserRole(request, deleteRoleResponseObserver);

        // Assert
        verify(deleteRoleResponseObserver).onNext(deleteRoleResponseCaptor.capture());
        verify(deleteRoleResponseObserver).onCompleted();

        DeleteUserRoleResponse response = deleteRoleResponseCaptor.getValue();
        assertTrue(response.getSuccess());
    }

    @Test
    void getRoleByName_Success() {
        // Arrange
        GetRoleByNameRequest request = GetRoleByNameRequest.newBuilder()
                .setRoleName("STAFF")
                .build();

        when(roleService.getRoleByRoleName("STAFF")).thenReturn(role);

        // Act
        userRoleService.getRoleByName(request, roleByNameResponseObserver);

        // Assert
        verify(roleByNameResponseObserver).onNext(roleByNameResponseCaptor.capture());
        verify(roleByNameResponseObserver).onCompleted();

        GetRoleByNameResponse response = roleByNameResponseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals("STAFF", response.getRoleName());
        assertEquals(roleId.toString(), response.getRoleId());
    }
}
