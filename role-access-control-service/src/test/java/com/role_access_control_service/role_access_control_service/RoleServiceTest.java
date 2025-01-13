package com.role_access_control_service.role_access_control_service;

import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.services.RoleAssignmentService;
import com.role_access_control_service.role_access_control_service.services.RoleService;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.BadRequestException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @InjectMocks
    private RoleService roleService;

    private UUID roleId;
    private Role role;
    private CreateRoleDto createRoleDto;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();

        role = Role.builder()
                .id(roleId)
                .roleName("STAFF")
                .createdAt(LocalDateTime.now())
                .build();

        createRoleDto = new CreateRoleDto();
        createRoleDto.setRoleName("Staff");
    }

    @Test
    void createRole_Success() {
        // Arrange
        when(roleRepository.findByRoleName("STAFF")).thenReturn(null);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // Act
        Role result = roleService.createRole(createRoleDto);

        // Assert
        assertNotNull(result);
        assertEquals("STAFF", result.getRoleName());
        verify(roleRepository, times(1)).findByRoleName("STAFF");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void createRole_ThrowsAlreadyExistsException() {
        // Arrange
        when(roleRepository.findByRoleName("STAFF")).thenReturn(role);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> roleService.createRole(createRoleDto));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getRoleById_Success() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // Act
        Role result = roleService.getRoleById(roleId);

        // Assert
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("STAFF", result.getRoleName());
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void getRoleById_ThrowsNotFoundException() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> roleService.getRoleById(roleId));
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void updateRole_Success() {
        // Arrange
        CreateRoleDto updateDto = new CreateRoleDto();
        updateDto.setRoleName("ADMIN");

        Role updatedRole = Role.builder()
                .id(roleId)
                .roleName("ADMIN")
                .createdAt(role.getCreatedAt())
                .build();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        // Act
        Role result = roleService.updateRole(roleId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.getRoleName());
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void updateRole_ThrowsNotFoundException() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> roleService.updateRole(roleId, createRoleDto));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void deleteRole_Success() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentService.checkAssignmentExistsByRoleId(roleId)).thenReturn(false);
        doNothing().when(roleRepository).deleteById(roleId);

        // Act
        roleService.deleteRole(roleId);

        // Assert
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleAssignmentService, times(1)).checkAssignmentExistsByRoleId(roleId);
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    @Test
    void deleteRole_ThrowsNotFoundException() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> roleService.deleteRole(roleId));
        verify(roleRepository, never()).deleteById(any());
        verify(roleAssignmentService, never()).checkAssignmentExistsByRoleId(any());
    }

    @Test
    void deleteRole_ThrowsBadRequestException() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentService.checkAssignmentExistsByRoleId(roleId)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> roleService.deleteRole(roleId));
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void getAllRoles_Success() {
        // Arrange
        List<Role> expectedRoles = Arrays.asList(role);
        when(roleRepository.findAll()).thenReturn(expectedRoles);

        // Act
        List<Role> result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void getRoleByRoleName_Success() {
        // Arrange
        when(roleRepository.findByRoleName("STAFF")).thenReturn(role);

        // Act
        Role result = roleService.getRoleByRoleName("STAFF");

        // Assert
        assertNotNull(result);
        assertEquals("STAFF", result.getRoleName());
        verify(roleRepository, times(1)).findByRoleName("STAFF");
    }

    @Test
    void getRoleByRoleName_ThrowsNotFoundException() {
        // Arrange
        when(roleRepository.findByRoleName("NONEXISTENT")).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> roleService.getRoleByRoleName("NONEXISTENT"));
        verify(roleRepository, times(1)).findByRoleName("NONEXISTENT");
    }
}