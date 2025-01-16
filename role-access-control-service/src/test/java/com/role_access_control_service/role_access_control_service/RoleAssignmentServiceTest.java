package com.role_access_control_service.role_access_control_service;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import com.role_access_control_service.role_access_control_service.repositories.RoleAssignmentRepository;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.services.RoleAssignmentService;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
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
class RoleAssignmentServiceTest {

    @Mock
    private RoleAssignmentRepository roleAssignmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleAssignmentService roleAssignmentService;

    private UUID userId;
    private UUID roleId;
    private UUID departmentId;
    private Role role;
    private RoleAssignment roleAssignment;
    private AssignRoleDto assignRoleDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        role = Role.builder()
                .id(roleId)
                .roleName("STAFF")
                .build();

        roleAssignment = RoleAssignment.builder()
                .userId(userId)
                .role(role)
                .departmentId(departmentId)
                .assignedAt(LocalDateTime.now())
                .build();

        assignRoleDto = new AssignRoleDto();
        assignRoleDto.setUserId(userId.toString());
        assignRoleDto.setRoleId(roleId.toString());
        assignRoleDto.setDepartmentId(departmentId.toString());
    }

    @Test
    void assignRole_Success() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentRepository.save(any(RoleAssignment.class))).thenReturn(roleAssignment);

        // Act
        RoleAssignment result = roleAssignmentService.assignRole(assignRoleDto);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(roleId, result.getRole().getId());
        assertEquals(departmentId, result.getDepartmentId());
        verify(roleAssignmentRepository, times(1)).save(any(RoleAssignment.class));
    }

    @Test
    void assignRole_HOD_Success() {
        // Arrange
        role.setRoleName("HOD");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId))
                .thenReturn(List.of());
        when(roleAssignmentRepository.save(any(RoleAssignment.class))).thenReturn(roleAssignment);

        // Act
        RoleAssignment result = roleAssignmentService.assignRole(assignRoleDto);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(roleAssignmentRepository, times(1)).save(any(RoleAssignment.class));
    }

    @Test
    void assignRole_HOD_ThrowsAlreadyExistsException() {
        // Arrange
        role.setRoleName("HOD");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId))
                .thenReturn(List.of(roleAssignment));

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> roleAssignmentService.assignRole(assignRoleDto));
        verify(roleAssignmentRepository, never()).save(any(RoleAssignment.class));
    }

    @Test
    void updateRoleAssignment_Success() {
        // Arrange
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentRepository.findByUserId(userId)).thenReturn(Optional.of(roleAssignment));
        when(roleAssignmentRepository.save(any(RoleAssignment.class))).thenReturn(roleAssignment);

        // Create a new role and dto for update
        UUID newRoleId = UUID.randomUUID();
        Role newRole = Role.builder().id(newRoleId).roleName("STAFF").build();
        assignRoleDto.setRoleId(newRoleId.toString());

        when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));

        // Act
        RoleAssignment result = roleAssignmentService.updateRoleAssignment(assignRoleDto);

        // Assert
        assertNotNull(result);
        verify(roleAssignmentRepository, times(1)).save(any(RoleAssignment.class));
    }

    @Test
    void getUserRoles_Success() {
        // Arrange
        when(roleAssignmentRepository.findByUserId(userId)).thenReturn(Optional.of(roleAssignment));

        // Act
        RoleAssignment result = roleAssignmentService.getUserRoles(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(roleAssignmentRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getUserRoles_ThrowsNotFoundException() {
        // Arrange
        when(roleAssignmentRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> roleAssignmentService.getUserRoles(userId));
        verify(roleAssignmentRepository, times(1)).findByUserId(userId);
    }

    @Test
    void deleteRoleAssignment_Success() {
        // Arrange
        when(roleAssignmentRepository.findByUserId(userId)).thenReturn(Optional.of(roleAssignment));
        doNothing().when(roleAssignmentRepository).delete(roleAssignment);

        // Act
        roleAssignmentService.deleteRoleAssignment(userId);

        // Assert
        verify(roleAssignmentRepository, times(1)).delete(roleAssignment);
    }

    @Test
    void getRoleAssignmentsByRoleIdAndDepartmentId_Success() {
        // Arrange
        List<RoleAssignment> expectedAssignments = Arrays.asList(roleAssignment);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId))
                .thenReturn(expectedAssignments);

        // Act
        List<RoleAssignment> result = roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(roleId, departmentId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(expectedAssignments.size(), result.size());
        verify(roleAssignmentRepository, times(1))
                .findByRoleIdAndDepartmentId(roleId, departmentId);
    }

    @Test
    void checkAssignmentExistsByRoleId_ReturnsTrue() {
        // Arrange
        when(roleAssignmentRepository.existsByRoleId(roleId)).thenReturn(true);

        // Act
        boolean result = roleAssignmentService.checkAssignmentExistsByRoleId(roleId);

        // Assert
        assertTrue(result);
        verify(roleAssignmentRepository, times(1)).existsByRoleId(roleId);
    }

    @Test
    void checkAssignmentExistsByRoleId_ReturnsFalse() {
        // Arrange
        when(roleAssignmentRepository.existsByRoleId(roleId)).thenReturn(false);

        // Act
        boolean result = roleAssignmentService.checkAssignmentExistsByRoleId(roleId);

        // Assert
        assertFalse(result);
        verify(roleAssignmentRepository, times(1)).existsByRoleId(roleId);
    }
}