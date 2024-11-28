package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopService.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    private UserRoleService userRoleService;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private RoleService roleService;

    @Mock
    private StreamObserver<GetRoleByUserIdResponse> getUserRoleObserver;

    @Mock
    private StreamObserver<AssignRoleResponse> assignRoleObserver;

    @Mock
    private StreamObserver<GetRoleAssignmentsByRoleIdAndDepartmentIdResponse> getRoleAssignmentsObserver;

    @Mock
    private StreamObserver<DeleteUserRoleResponse> deleteUserRoleObserver;

    @Mock
    private StreamObserver<GetRoleByNameResponse> getRoleByNameObserver;

    // Test data constants
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_ROLE_ID = UUID.randomUUID();
    private static final UUID TEST_DEPARTMENT_ID = UUID.randomUUID();
    private static final String TEST_ROLE_NAME = "TEST_ROLE";

    @BeforeEach
    void setUp() {
        userRoleService = new UserRoleService(roleAssignmentService, roleService);
    }

    @Nested
    class GetRoleAssignmentsByUserId {
        @Test
        void shouldSuccessfullyGetRoleAssignmentForUser() {
            // Given
            RoleAssignment mockRoleAssignment = createMockRoleAssignment();
            given(roleAssignmentService.getRoleAssignmentByUserId(TEST_USER_ID))
                    .willReturn(mockRoleAssignment);

            GetRoleByUserIdRequest request = GetRoleByUserIdRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .build();

            // When
            userRoleService.getRoleAssignmentsByUserId(request, getUserRoleObserver);

            // Then
            ArgumentCaptor<GetRoleByUserIdResponse> responseCaptor =
                    ArgumentCaptor.forClass(GetRoleByUserIdResponse.class);
            verify(getUserRoleObserver).onNext(responseCaptor.capture());
            verify(getUserRoleObserver).onCompleted();

            GetRoleByUserIdResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getRoleName()).isEqualTo(TEST_ROLE_NAME);
            assertThat(response.getRoleId()).isEqualTo(TEST_ROLE_ID.toString());
        }

        @Test
        void shouldHandleErrorWhenGettingRoleAssignmentFails() {
            // Given
            GetRoleByUserIdRequest request = GetRoleByUserIdRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .build();

            given(roleAssignmentService.getRoleAssignmentByUserId(TEST_USER_ID))
                    .willThrow(new RuntimeException("Test error"));

            // When
            userRoleService.getRoleAssignmentsByUserId(request, getUserRoleObserver);

            // Then
            ArgumentCaptor<GetRoleByUserIdResponse> responseCaptor =
                    ArgumentCaptor.forClass(GetRoleByUserIdResponse.class);
            verify(getUserRoleObserver).onNext(responseCaptor.capture());
            verify(getUserRoleObserver).onCompleted();

            GetRoleByUserIdResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("Test error");
        }
    }

    @Nested
    class AssignRole {
        @Test
        void shouldSuccessfullyAssignRole() {
            // Given
            RoleAssignment mockRoleAssignment = createMockRoleAssignment();
            given(roleAssignmentService.assignRole(any(AssignRoleDto.class)))
                    .willReturn(mockRoleAssignment);

            AssignRoleRequest request = AssignRoleRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .setRoleId(TEST_ROLE_ID.toString())
                    .setDepartmentId(TEST_DEPARTMENT_ID.toString())
                    .build();

            // When
            userRoleService.assignRole(request, assignRoleObserver);

            // Then
            ArgumentCaptor<AssignRoleResponse> responseCaptor =
                    ArgumentCaptor.forClass(AssignRoleResponse.class);
            verify(assignRoleObserver).onNext(responseCaptor.capture());
            verify(assignRoleObserver).onCompleted();

            AssignRoleResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getAssignmentId()).isEqualTo(mockRoleAssignment.getId().toString());
        }

        @Test
        void shouldHandleErrorWhenAssigningRoleFails() {
            // Given
            AssignRoleRequest request = AssignRoleRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .setRoleId(TEST_ROLE_ID.toString())
                    .setDepartmentId(TEST_DEPARTMENT_ID.toString())
                    .build();

            given(roleAssignmentService.assignRole(any(AssignRoleDto.class)))
                    .willThrow(new RuntimeException("Assignment failed"));

            // When
            userRoleService.assignRole(request, assignRoleObserver);

            // Then
            ArgumentCaptor<AssignRoleResponse> responseCaptor =
                    ArgumentCaptor.forClass(AssignRoleResponse.class);
            verify(assignRoleObserver).onNext(responseCaptor.capture());
            verify(assignRoleObserver).onCompleted();

            AssignRoleResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("Assignment failed");
        }
    }
    @Nested
    class UpdateUserRole {
        @Test
        void shouldSuccessfullyUpdateUserRole() {
            // Given
            RoleAssignment mockRoleAssignment = createMockRoleAssignment();
            given(roleAssignmentService.updateRoleAssignment(any(AssignRoleDto.class)))
                    .willReturn(mockRoleAssignment);

            AssignRoleRequest request = AssignRoleRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .setRoleId(TEST_ROLE_ID.toString())
                    .setDepartmentId(TEST_DEPARTMENT_ID.toString())
                    .build();

            // When
            userRoleService.updateUserRole(request, assignRoleObserver);

            // Then
            ArgumentCaptor<AssignRoleResponse> responseCaptor =
                    ArgumentCaptor.forClass(AssignRoleResponse.class);
            verify(assignRoleObserver).onNext(responseCaptor.capture());
            verify(assignRoleObserver).onCompleted();

            AssignRoleResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getAssignmentId()).isEqualTo(mockRoleAssignment.getId().toString());
        }

        @Test
        void shouldHandleErrorWhenUpdatingUserRoleFails() {
            // Given
            AssignRoleRequest request = AssignRoleRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .setRoleId(TEST_ROLE_ID.toString())
                    .setDepartmentId(TEST_DEPARTMENT_ID.toString())
                    .build();

            given(roleAssignmentService.updateRoleAssignment(any(AssignRoleDto.class)))
                    .willThrow(new RuntimeException("Update failed"));

            // When
            userRoleService.updateUserRole(request, assignRoleObserver);

            // Then
            ArgumentCaptor<AssignRoleResponse> responseCaptor =
                    ArgumentCaptor.forClass(AssignRoleResponse.class);
            verify(assignRoleObserver).onNext(responseCaptor.capture());
            verify(assignRoleObserver).onCompleted();

            AssignRoleResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("Update failed");
        }
    }
    @Nested
    class GetRoleAssignmentsByRoleIdAndDepartmentId {
        @Test
        void shouldSuccessfullyGetRoleAssignments() {
            // Given
            List<RoleAssignment> mockRoleAssignments = Arrays.asList(
                    createMockRoleAssignment(),
                    createMockRoleAssignment()
            );
            given(roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(TEST_ROLE_ID, TEST_DEPARTMENT_ID))
                    .willReturn(mockRoleAssignments);

            GetRoleAssignmentsByRoleIdAndDepartmentIdRequest request =
                    GetRoleAssignmentsByRoleIdAndDepartmentIdRequest.newBuilder()
                            .setRoleId(TEST_ROLE_ID.toString())
                            .setDepartmentId(TEST_DEPARTMENT_ID.toString())
                            .build();

            // When
            userRoleService.getRoleAssignmentsByRoleIdAndDepartmentId(request, getRoleAssignmentsObserver);

            // Then
            ArgumentCaptor<GetRoleAssignmentsByRoleIdAndDepartmentIdResponse> responseCaptor =
                    ArgumentCaptor.forClass(GetRoleAssignmentsByRoleIdAndDepartmentIdResponse.class);
            verify(getRoleAssignmentsObserver).onNext(responseCaptor.capture());
            verify(getRoleAssignmentsObserver).onCompleted();

            GetRoleAssignmentsByRoleIdAndDepartmentIdResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getUserIdsList()).hasSize(2);
        }

        @Test
        void shouldHandleErrorWhenGettingRoleAssignmentsFails() {
            // Given
            GetRoleAssignmentsByRoleIdAndDepartmentIdRequest request =
                    GetRoleAssignmentsByRoleIdAndDepartmentIdRequest.newBuilder()
                            .setRoleId(TEST_ROLE_ID.toString())
                            .setDepartmentId(TEST_DEPARTMENT_ID.toString())
                            .build();

            given(roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(TEST_ROLE_ID, TEST_DEPARTMENT_ID))
                    .willThrow(new RuntimeException("Retrieval failed"));

            // When
            userRoleService.getRoleAssignmentsByRoleIdAndDepartmentId(request, getRoleAssignmentsObserver);

            // Then
            ArgumentCaptor<GetRoleAssignmentsByRoleIdAndDepartmentIdResponse> responseCaptor =
                    ArgumentCaptor.forClass(GetRoleAssignmentsByRoleIdAndDepartmentIdResponse.class);
            verify(getRoleAssignmentsObserver).onNext(responseCaptor.capture());
            verify(getRoleAssignmentsObserver).onCompleted();

            GetRoleAssignmentsByRoleIdAndDepartmentIdResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("Retrieval failed");
        }
    }

    @Nested
    class DeleteUserRole {
        @Test
        void shouldSuccessfullyDeleteUserRole() {
            // Given
            DeleteUserRoleRequest request = DeleteUserRoleRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .build();

            // When
            userRoleService.deleteUserRole(request, deleteUserRoleObserver);

            // Then
            ArgumentCaptor<DeleteUserRoleResponse> responseCaptor =
                    ArgumentCaptor.forClass(DeleteUserRoleResponse.class);
            verify(deleteUserRoleObserver).onNext(responseCaptor.capture());
            verify(deleteUserRoleObserver).onCompleted();

            DeleteUserRoleResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isTrue();
            verify(roleAssignmentService).deleteRoleAssignment(TEST_USER_ID);
        }

        @Test
        void shouldHandleErrorWhenDeletingUserRoleFails() {
            // Given
            DeleteUserRoleRequest request = DeleteUserRoleRequest.newBuilder()
                    .setUserId(TEST_USER_ID.toString())
                    .build();

            doThrow(new RuntimeException("Deletion failed"))
                    .when(roleAssignmentService).deleteRoleAssignment(TEST_USER_ID);

            // When
            userRoleService.deleteUserRole(request, deleteUserRoleObserver);

            // Then
            ArgumentCaptor<DeleteUserRoleResponse> responseCaptor =
                    ArgumentCaptor.forClass(DeleteUserRoleResponse.class);
            verify(deleteUserRoleObserver).onNext(responseCaptor.capture());
            verify(deleteUserRoleObserver).onCompleted();

            DeleteUserRoleResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("Deletion failed");
        }
    }

    @Nested
    class GetRoleByName {
        @Test
        void shouldSuccessfullyGetRoleByName() {
            // Given
            Role mockRole = createMockRole();
            given(roleService.getRoleByRoleName(TEST_ROLE_NAME))
                    .willReturn(mockRole);

            GetRoleByNameRequest request = GetRoleByNameRequest.newBuilder()
                    .setRoleName(TEST_ROLE_NAME)
                    .build();

            // When
            userRoleService.getRoleByName(request, getRoleByNameObserver);

            // Then
            ArgumentCaptor<GetRoleByNameResponse> responseCaptor =
                    ArgumentCaptor.forClass(GetRoleByNameResponse.class);
            verify(getRoleByNameObserver).onNext(responseCaptor.capture());
            verify(getRoleByNameObserver).onCompleted();

            GetRoleByNameResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getRoleName()).isEqualTo(TEST_ROLE_NAME);
            assertThat(response.getRoleId()).isEqualTo(TEST_ROLE_ID.toString());
        }

        @Test
        void shouldHandleErrorWhenGettingRoleByNameFails() {
            // Given
            GetRoleByNameRequest request = GetRoleByNameRequest.newBuilder()
                    .setRoleName(TEST_ROLE_NAME)
                    .build();

            given(roleService.getRoleByRoleName(TEST_ROLE_NAME))
                    .willThrow(new RuntimeException("Role not found"));

            // When
            userRoleService.getRoleByName(request, getRoleByNameObserver);

            // Then
            ArgumentCaptor<GetRoleByNameResponse> responseCaptor =
                    ArgumentCaptor.forClass(GetRoleByNameResponse.class);
            verify(getRoleByNameObserver).onNext(responseCaptor.capture());
            verify(getRoleByNameObserver).onCompleted();

            GetRoleByNameResponse response = responseCaptor.getValue();
            assertThat(response.getSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("Role not found");
        }
    }

    // Helper methods for creating mock objects
    private RoleAssignment createMockRoleAssignment() {
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setId(UUID.randomUUID());
        roleAssignment.setUserId(TEST_USER_ID);
        roleAssignment.setDepartmentId(TEST_DEPARTMENT_ID);

        Role role = createMockRole();
        roleAssignment.setRole(role);

        return roleAssignment;
    }

    private Role createMockRole() {
        Role role = new Role();
        role.setId(TEST_ROLE_ID);
        role.setRoleName(TEST_ROLE_NAME);
        return role;
    }
}