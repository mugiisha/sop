package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
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
    private static final String TEST_ROLE_NAME = "TEST ROLE";
    private static final String UPDATED_ROLE_NAME = "UPDATED ROLE";

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    @Nested
    class CreateRole {
        @Test
        void shouldSuccessfullyCreateRole() throws AlreadyExistsException {
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

        @Test
        void shouldThrowExceptionWhenRoleAlreadyExists() {
            // Given
            CreateRoleDto createRoleDto = new CreateRoleDto(TEST_ROLE_NAME);
            Role existingRole = createTestRole();
            given(roleRepository.findByRoleName(TEST_ROLE_NAME.toUpperCase())).willReturn(existingRole);

            // When/Then
            assertThatThrownBy(() -> roleService.createRole(createRoleDto))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessageContaining("Role already exists");
        }
    }

    @Nested
    class GetRoles {
        @Test
        void shouldSuccessfullyGetAllRoles() {
            // When
            roleService.getAllRoles();

            // Then
            verify(roleRepository).findAll();
        }

        @Test
        void shouldSuccessfullyGetRoleById() throws NotFoundException {
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
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class GetRoleByRoleName {
        @Test
        void shouldSuccessfullyGetRoleByRoleName() throws NotFoundException {
            // Given
            Role mockRole = createTestRole();
            given(roleRepository.findByRoleName(TEST_ROLE_NAME)).willReturn(mockRole);

            // When
            Role role = roleService.getRoleByRoleName(TEST_ROLE_NAME);

            // Then
            assertThat(role).isEqualTo(mockRole);
        }

        @Test
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            given(roleRepository.findByRoleName(TEST_ROLE_NAME)).willReturn(null);

            // When/Then
            assertThatThrownBy(() -> roleService.getRoleByRoleName(TEST_ROLE_NAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Role not found");
        }
    }

    @Nested
    class UpdateRole {
        @Test
        void shouldSuccessfullyUpdateRole() throws NotFoundException {
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
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class DeleteRole {
        @Test
        void shouldSuccessfullyDeleteRole() throws NotFoundException {
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
                    .isInstanceOf(NotFoundException.class);

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