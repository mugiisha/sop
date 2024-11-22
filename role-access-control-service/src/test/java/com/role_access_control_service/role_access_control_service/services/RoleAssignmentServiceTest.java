package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import com.role_access_control_service.role_access_control_service.repositories.RoleAssignmentRepository;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {

    private RoleAssignmentService roleAssignmentService;

    @Mock
    private RoleAssignmentRepository roleAssignmentRepository;

    @Mock
    private RoleRepository roleRepository;

    // Test data constants
    private static final UUID TEST_ROLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID TEST_DEPARTMENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final LocalDateTime dateTime = LocalDateTime.parse("2024-12-31T23:59:59");

    @BeforeEach
    void setUp() {
        roleAssignmentService = new RoleAssignmentService(roleAssignmentRepository, roleRepository);
    }

    @Nested
    class AssignRole {
        @Test
        void shouldSuccessfullyAssignRole() throws CustomException {
            // Given
            Role testRole = new Role(TEST_ROLE_ID, "test role");
            AssignRoleDto assignRoleDto = createTestAssignRoleDto();

            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(testRole));

            // When
            roleAssignmentService.assignRole(assignRoleDto);

            // Then
            ArgumentCaptor<RoleAssignment> roleAssignmentCaptor = ArgumentCaptor.forClass(RoleAssignment.class);
            verify(roleAssignmentRepository).save(roleAssignmentCaptor.capture());

            RoleAssignment capturedAssignment = roleAssignmentCaptor.getValue();
            assertThat(capturedAssignment)
                    .satisfies(assignment -> {
                        assertThat(assignment.getUserId()).isEqualTo(TEST_USER_ID);
                        assertThat(assignment.getRole().getId()).isEqualTo(TEST_ROLE_ID);
                        assertThat(assignment.getDepartmentId()).isEqualTo(TEST_DEPARTMENT_ID);
                        assertThat(assignment.getAssignedAt()).isEqualTo(dateTime);
                    });
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            AssignRoleDto assignRoleDto = createTestAssignRoleDto();
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.assignRole(assignRoleDto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("Role not found");
        }
    }

    @Nested
    class GetAssignments {
        @Test
        void shouldReturnUserAssignments() throws CustomException {
            // Given
            RoleAssignment expectedAssignments = createTestRoleAssignment();
            given(roleAssignmentRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(expectedAssignments));

            // When
            Optional<RoleAssignment> actualAssignment = roleAssignmentService.getRoleAssignmentByUserId(TEST_USER_ID);

            // Then
            assertThat(actualAssignment)
                    .isSameAs(expectedAssignments);
        }
    }

    @Nested
    class UpdateAssignment {
        @Test
        void shouldSuccessfullyUpdateAssignment() throws CustomException {
            // Given
            UUID assignmentId = UUID.randomUUID();
            Role newRole = new Role(TEST_ROLE_ID, "new role");
            AssignRoleDto updateDto = createTestAssignRoleDto();
            RoleAssignment existingAssignment = createTestRoleAssignment();

            given(roleAssignmentRepository.findById(assignmentId))
                    .willReturn(Optional.of(existingAssignment));

            // When
            RoleAssignment updatedAssignment = roleAssignmentService.updateRoleAssignment(updateDto);

            // Then
            assertThat(updatedAssignment)
                    .satisfies(assignment -> {
                        assertThat(assignment.getUserId()).isEqualTo(TEST_USER_ID);
                        assertThat(assignment.getRole().getId()).isEqualTo(newRole.getId());
                        assertThat(assignment.getDepartmentId()).isEqualTo(TEST_DEPARTMENT_ID);
                        assertThat(assignment.getAssignedAt()).isEqualTo(dateTime);
                    });
        }

        @Test
        void shouldThrowExceptionWhenAssignmentNotFound() {
            // Given
            UUID assignmentId = UUID.randomUUID();
            AssignRoleDto updateDto = createTestAssignRoleDto();
            given(roleAssignmentRepository.findById(assignmentId))
                    .willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.updateRoleAssignment(updateDto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("Role assignment not found");
        }
    }

    @Nested
    class DeleteAssignment {
        @Test
        void shouldSuccessfullyDeleteAssignment() throws CustomException {
            // Given
            UUID assignmentId = UUID.randomUUID();
            RoleAssignment existingAssignment = createTestRoleAssignment();
            given(roleAssignmentRepository.findById(assignmentId))
                    .willReturn(Optional.of(existingAssignment));

            // When
            roleAssignmentService.deleteRoleAssignment(assignmentId);

            // Then
            verify(roleAssignmentRepository).delete(existingAssignment);
        }

        @Test
        void shouldThrowExceptionWhenAssignmentNotFoundForDeletion() {
            // Given
            UUID assignmentId = UUID.randomUUID();
            given(roleAssignmentRepository.findById(assignmentId))
                    .willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.deleteRoleAssignment(assignmentId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("Role assignment not found");

            verify(roleAssignmentRepository, never()).delete(any());
        }
    }

    // Helper methods
    private AssignRoleDto createTestAssignRoleDto() {
        return new AssignRoleDto(
                TEST_USER_ID.toString(),
                TEST_ROLE_ID.toString(),
                TEST_DEPARTMENT_ID.toString(),
                dateTime
        );
    }

    private RoleAssignment createTestRoleAssignment() {
        RoleAssignment assignment = new RoleAssignment();
        assignment.setUserId(TEST_USER_ID);
        assignment.setRole(new Role(TEST_ROLE_ID, "test role"));
        assignment.setDepartmentId(TEST_DEPARTMENT_ID);
        assignment.setAssignedAt(dateTime);
        return assignment;
    }
}