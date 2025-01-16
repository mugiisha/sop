package com.user_management_service.user_management_service.service.services;

import com.user_management_service.user_management_service.exceptions.DepartmentAlreadyExistsException;
import com.user_management_service.user_management_service.exceptions.ResourceNotFoundException;
import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.dtos.DepartmentCreationDTO;
import com.user_management_service.user_management_service.dtos.DepartmentDTO;
import com.user_management_service.user_management_service.dtos.DepartmentUpdateDTO;
import com.user_management_service.user_management_service.repositories.DepartmentRepository;
import com.user_management_service.user_management_service.services.DepartmentService;
import com.user_management_service.user_management_service.services.UserService;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DepartmentService departmentService;

    private UUID departmentId;
    private Department department;
    private DepartmentCreationDTO creationDTO;
    private DepartmentUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();

        department = new Department();
        department.setId(departmentId);
        department.setName("HR");
        department.setDescription("Human Resources");
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        creationDTO = new DepartmentCreationDTO();
        creationDTO.setName("HR");
        creationDTO.setDescription("Human Resources");

        updateDTO = new DepartmentUpdateDTO();
        updateDTO.setName("Updated HR");
        updateDTO.setDescription("Updated Human Resources");
    }

    @Test
    void createDepartment_Success() {
        // Arrange
        when(departmentRepository.existsByName(creationDTO.getName())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(userService.getDepartmentUsersCount(departmentId)).thenReturn(0L);

        // Act
        DepartmentDTO result = departmentService.createDepartment(creationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(department.getName(), result.getName());
        assertEquals(department.getDescription(), result.getDescription());
        verify(departmentRepository).existsByName(creationDTO.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_DepartmentAlreadyExists() {
        // Arrange
        when(departmentRepository.existsByName(creationDTO.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(DepartmentAlreadyExistsException.class,
                () -> departmentService.createDepartment(creationDTO));
        verify(departmentRepository).existsByName(creationDTO.getName());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void getDepartmentById_Success() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userService.getDepartmentUsersCount(departmentId)).thenReturn(5L);

        // Act
        DepartmentDTO result = departmentService.getDepartmentById(departmentId);

        // Assert
        assertNotNull(result);
        assertEquals(department.getId(), result.getId());
        assertEquals(department.getName(), result.getName());
        assertEquals(5L, result.getStaff());
        verify(departmentRepository).findById(departmentId);
    }

    @Test
    void getDepartmentById_NotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(departmentId));
        verify(departmentRepository).findById(departmentId);
    }

    @Test
    void getAllDepartments_Success() {
        // Arrange
        Department dept2 = new Department();
        dept2.setId(UUID.randomUUID());
        dept2.setName("IT");
        dept2.setDescription("Information Technology");

        List<Department> departments = Arrays.asList(department, dept2);
        when(departmentRepository.findAll()).thenReturn(departments);
        when(userService.getDepartmentUsersCount(any(UUID.class))).thenReturn(5L);

        // Act
        List<DepartmentDTO> result = departmentService.getAllDepartments();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(department.getName(), result.get(0).getName());
        assertEquals(dept2.getName(), result.get(1).getName());
        verify(departmentRepository).findAll();
    }

    @Test
    void updateDepartment_Success() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(updateDTO.getName())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(userService.getDepartmentUsersCount(departmentId)).thenReturn(5L);

        // Act
        DepartmentDTO result = departmentService.updateDepartment(departmentId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void updateDepartment_NotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(departmentId, updateDTO));
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_NameAlreadyExists() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(updateDTO.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(DepartmentAlreadyExistsException.class,
                () -> departmentService.updateDepartment(departmentId, updateDTO));
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void deleteDepartment_Success() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        // Act
        departmentService.deleteDepartment(departmentId);

        // Assert
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartment_NotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(departmentId));
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository, never()).delete(any(Department.class));
    }
}