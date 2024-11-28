package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import com.role_access_control_service.role_access_control_service.repositories.RoleAssignmentRepository;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
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
        void shouldSuccessfullyAssignRole() {
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
                    });
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            AssignRoleDto assignRoleDto = createTestAssignRoleDto();
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.assignRole(assignRoleDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Role not found");
        }

        @Test
        void shouldThrowExceptionWhenHodAlreadyExistsInDepartment() {
            // Given
            Role hodRole = new Role(TEST_ROLE_ID, "HOD");
            AssignRoleDto assignRoleDto = createTestAssignRoleDto();
            RoleAssignment existingHodAssignment = createTestRoleAssignment();
            existingHodAssignment.setRole(hodRole);

            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(hodRole));
            given(roleAssignmentRepository.findByRoleIdAndDepartmentId(TEST_ROLE_ID, TEST_DEPARTMENT_ID))
                    .willReturn(List.of(existingHodAssignment));

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.assignRole(assignRoleDto))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessageContaining("A department can't have more than one HOD");
        }
    }

    @Nested
    class GetAssignments {
        @Test
        void shouldReturnUserAssignments() throws NotFoundException {
            // Given
            RoleAssignment expectedAssignments = createTestRoleAssignment();
            given(roleAssignmentRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(expectedAssignments));

            // When
            RoleAssignment actualAssignment = roleAssignmentService.getRoleAssignmentByUserId(TEST_USER_ID);

            // Then
            assertThat(actualAssignment)
                    .isSameAs(expectedAssignments);
        }

        @Test
        void shouldReturnRoleAssignmentsByRoleIdAndDepartmentId() throws NotFoundException {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID departmentId = UUID.randomUUID();
            Role role = new Role(roleId, "test role");
            RoleAssignment roleAssignment = createTestRoleAssignment();
            roleAssignment.setRole(role);
            roleAssignment.setDepartmentId(departmentId);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId))
                    .willReturn(List.of(roleAssignment));

            // When
            List<RoleAssignment> assignments = roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(roleId, departmentId);

            // Then
            assertThat(assignments).containsExactly(roleAssignment);
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID departmentId = UUID.randomUUID();

            given(roleRepository.findById(roleId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(roleId, departmentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Role not found");
        }

        @Test
        void shouldReturnRoleAssignmentsByDepartmentId() {
            // Given
            UUID departmentId = UUID.randomUUID();
            RoleAssignment roleAssignment = createTestRoleAssignment();
            roleAssignment.setDepartmentId(departmentId);

            given(roleAssignmentRepository.findByDepartmentId(departmentId))
                    .willReturn(List.of(roleAssignment));

            // When
            List<RoleAssignment> assignments = roleAssignmentService.getRoleAssignmentsByDepartmentId(departmentId);

            // Then
            assertThat(assignments).containsExactly(roleAssignment);
        }
    }

    @Nested
    class UpdateAssignment {
//        @Test
//        void shouldSuccessfullyUpdateAssignment() throws NotFoundException {
//            // Given
//            Role newRole = new Role(TEST_ROLE_ID, "NEW ROLE");
//            AssignRoleDto updateDto = createTestAssignRoleDto();
//            RoleAssignment existingAssignment = createTestRoleAssignment();
//
//            given(roleAssignmentRepository.findByUserId(existingAssignment.getUserId()))
//                    .willReturn(Optional.of(existingAssignment));
//
//            // When
//            RoleAssignment updatedAssignment = roleAssignmentService.updateRoleAssignment(updateDto);
//
//            // Then
//            assertThat(updatedAssignment)
//                    .satisfies(assignment -> {
//                        assertThat(assignment.getUserId()).isEqualTo(TEST_USER_ID);
//                        assertThat(assignment.getRole().getId()).isEqualTo(newRole.getId());
//                        assertThat(assignment.getDepartmentId()).isEqualTo(TEST_DEPARTMENT_ID);
//                        assertThat(assignment.getAssignedAt()).isEqualTo(dateTime);
//                    });
//        }

        @Test
        void shouldSuccessfullyUpdateAssignment() throws NotFoundException, AlreadyExistsException {
            // Given
            Role newRole = new Role(TEST_ROLE_ID, "NEW ROLE");
            AssignRoleDto updateDto = createTestAssignRoleDto();
            RoleAssignment existingAssignment = createTestRoleAssignment();
            existingAssignment.setRole(new Role(UUID.randomUUID(), "OLD ROLE"));

            given(roleAssignmentRepository.findByUserId(TEST_USER_ID)).willReturn(Optional.of(existingAssignment));
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(newRole));

            // When
            RoleAssignment updatedAssignment = roleAssignmentService.updateRoleAssignment(updateDto);

            // Then
            ArgumentCaptor<RoleAssignment> roleAssignmentCaptor = ArgumentCaptor.forClass(RoleAssignment.class);
            verify(roleAssignmentRepository).save(roleAssignmentCaptor.capture());

            RoleAssignment capturedAssignment = roleAssignmentCaptor.getValue();
            assertThat(capturedAssignment)
                    .satisfies(assignment -> {
                        assertThat(assignment.getUserId()).isEqualTo(TEST_USER_ID);
                        assertThat(assignment.getRole().getId()).isEqualTo(newRole.getId());
                        assertThat(assignment.getDepartmentId()).isEqualTo(TEST_DEPARTMENT_ID);
                    });
        }

        @Test
        void shouldThrowExceptionWhenAssignmentNotFound() {
            // Given
            AssignRoleDto updateDto = createTestAssignRoleDto();
            given(roleAssignmentRepository.findByUserId(UUID.fromString(updateDto.getUserId())))
                    .willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.updateRoleAssignment(updateDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Role assignment not found");
        }
    }

    @Nested
    class DeleteAssignment {
        @Test
        void shouldSuccessfullyDeleteAssignment() throws NotFoundException {
            // Given
            UUID userId = UUID.randomUUID();
            RoleAssignment existingAssignment = createTestRoleAssignment();
            given(roleAssignmentRepository.findByUserId(userId))
                    .willReturn(Optional.of(existingAssignment));

            // When
            roleAssignmentService.deleteRoleAssignment(userId);

            // Then
            verify(roleAssignmentRepository).delete(existingAssignment);
        }

        @Test
        void shouldThrowExceptionWhenAssignmentNotFoundForDeletion() {
            // Given
            UUID userId = UUID.randomUUID();
            given(roleAssignmentRepository.findByUserId(userId))
                    .willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleAssignmentService.deleteRoleAssignment(userId))
                    .isInstanceOf(NotFoundException.class)
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