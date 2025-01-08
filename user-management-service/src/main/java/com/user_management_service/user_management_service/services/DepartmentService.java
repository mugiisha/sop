package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.exceptions.ResourceNotFoundException;
import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.dtos.DepartmentCreationDTO;
import com.user_management_service.user_management_service.dtos.DepartmentDTO;
import com.user_management_service.user_management_service.dtos.DepartmentUpdateDTO;
import com.user_management_service.user_management_service.exceptions.DepartmentAlreadyExistsException;
import com.user_management_service.user_management_service.repositories.DepartmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentDTO createDepartment(DepartmentCreationDTO creationDTO) {
        log.info("Creating new department with name: {}", creationDTO.getName());

        if (departmentRepository.existsByName(creationDTO.getName())) {
            throw new DepartmentAlreadyExistsException("Department with name " + creationDTO.getName() + " already exists");
        }

        Department department = new Department();
        department.setName(creationDTO.getName());
        department.setDescription(creationDTO.getDescription());
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        log.debug("Initial department timestamps - createdAt: {}, updatedAt: {}",
                department.getCreatedAt(), department.getUpdatedAt());

        Department savedDepartment = departmentRepository.save(department);

        log.info("Department created successfully with ID: {}", savedDepartment.getId());
        log.debug("Saved department timestamps - createdAt: {}, updatedAt: {}",
                savedDepartment.getCreatedAt(), savedDepartment.getUpdatedAt());

        return convertToDTO(savedDepartment);
    }

    @Cacheable(value = "department", key = "#id")
    public DepartmentDTO getDepartmentById(UUID id) {
        log.info("Fetching department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return convertToDTO(department);
    }

    @Cacheable(value = "departments")
    public List<DepartmentDTO> getAllDepartments() {
        log.info("Fetching all departments");
        List<Department> departments = departmentRepository.findAll();
        log.debug("Found {} departments", departments.size());
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "department", allEntries = true),
            @CacheEvict(value = "departments", allEntries = true)
    })
    public DepartmentDTO updateDepartment(UUID id, DepartmentUpdateDTO updateDTO) {
        log.info("Updating department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (!department.getName().equals(updateDTO.getName()) &&
                departmentRepository.existsByName(updateDTO.getName())) {
            throw new DepartmentAlreadyExistsException("Department with name " + updateDTO.getName() + " already exists");
        }

        department.setName(updateDTO.getName());
        department.setDescription(updateDTO.getDescription());
        department.setUpdatedAt(LocalDateTime.now());

        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated successfully");
        log.debug("Updated department timestamps - createdAt: {}, updatedAt: {}",
                updatedDepartment.getCreatedAt(), updatedDepartment.getUpdatedAt());

        return convertToDTO(updatedDepartment);
    }

    @Caching(evict = {
            @CacheEvict(value = "department", allEntries = true),
            @CacheEvict(value = "departments", allEntries = true)
    })
    public void deleteDepartment(UUID id) {
        log.info("Deleting department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        departmentRepository.delete(department);
        log.info("Department deleted successfully");
    }

    private DepartmentDTO convertToDTO(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setUpdatedAt(department.getUpdatedAt());

        log.debug("Converting to DTO - Department ID: {}, createdAt: {}, updatedAt: {}",
                department.getId(), dto.getCreatedAt(), dto.getUpdatedAt());

        return dto;
    }

    private Department convertToEntity(DepartmentDTO dto) {
        if (dto == null) {
            return null;
        }

        Department department = new Department();
        department.setId(dto.getId());
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setCreatedAt(dto.getCreatedAt());
        department.setUpdatedAt(dto.getUpdatedAt());

        return department;
    }
}