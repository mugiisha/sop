package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    // Test data constants
    private static final UUID TEST_ROLE_ID = UUID.randomUUID();
    private static final String TEST_ROLE_NAME = "Test role";
    private static final String UPDATED_ROLE_NAME = "Updated role";

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    @Nested
    class CreateRole {
        @Test
        void shouldSuccessfullyCreateRole() throws CustomException {
            // Given
            CreateRoleDto createRoleDto = new CreateRoleDto(TEST_ROLE_NAME);

            // When
            roleService.createRole(createRoleDto);

            // Then
            ArgumentCaptor<Role> roleArgumentCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(roleArgumentCaptor.capture());

            Role capturedRole = roleArgumentCaptor.getValue();
            assertThat(capturedRole.getRoleName()).isEqualTo(createRoleDto.getRoleName());
        }
    }

    @Nested
    class GetRoles {
        @Test
        void shouldSuccessfullyGetAllRoles() throws CustomException {
            // When
            roleService.getAllRoles();

            // Then
            verify(roleRepository).findAll();
        }

        @Test
        void shouldSuccessfullyGetRoleById() throws Exception {
            // Given
            Role mockRole = createTestRole();
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(mockRole));

            // When
            Role role = roleService.getRoleById(TEST_ROLE_ID);

            // Then
            assertThat(role).isEqualTo(mockRole);
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleService.getRoleById(TEST_ROLE_ID))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class UpdateRole {
        @Test
        void shouldSuccessfullyUpdateRole() throws Exception {
            // Given
            CreateRoleDto updateRoleDto = new CreateRoleDto(UPDATED_ROLE_NAME);
            Role existingRole = createTestRole();

            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(existingRole));
            when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

            // When
            Role updatedRole = roleService.updateRole(TEST_ROLE_ID, updateRoleDto);

            // Then
            assertThat(updatedRole.getRoleName()).isEqualTo(updateRoleDto.getRoleName());
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            CreateRoleDto updateRoleDto = new CreateRoleDto(UPDATED_ROLE_NAME);
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleService.updateRole(TEST_ROLE_ID, updateRoleDto))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class DeleteRole {
        @Test
        void shouldSuccessfullyDeleteRole() throws Exception {
            // Given
            Role existingRole = createTestRole();
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(existingRole));

            // When
            roleService.deleteRole(TEST_ROLE_ID);

            // Then
            verify(roleRepository).deleteById(TEST_ROLE_ID);
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            given(roleRepository.findById(TEST_ROLE_ID)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> roleService.deleteRole(TEST_ROLE_ID))
                    .isInstanceOf(CustomException.class);

            verify(roleRepository, never()).deleteById(any());
        }
    }

    // Helper methods
    private Role createTestRole() {
        Role role = new Role();
        role.setRoleName(TEST_ROLE_NAME);
        return role;
    }
}