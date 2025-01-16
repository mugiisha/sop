package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.enums.ErrorCode;
import com.user_management_service.user_management_service.exceptions.DepartmentValidationException;
import com.user_management_service.user_management_service.exceptions.ResourceNotFoundException;
import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.dtos.DepartmentCreationDTO;
import com.user_management_service.user_management_service.dtos.DepartmentDTO;
import com.user_management_service.user_management_service.dtos.DepartmentUpdateDTO;
import com.user_management_service.user_management_service.exceptions.DepartmentAlreadyExistsException;
import com.user_management_service.user_management_service.exceptions.DepartmentNotEmptyException;
import com.user_management_service.user_management_service.repositories.DepartmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final UserService userService;

    private static final int MAX_DEPARTMENT_NAME_LENGTH = 100;
    private static final int MAX_DEPARTMENT_DESCRIPTION_LENGTH = 500;

    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentDTO createDepartment(DepartmentCreationDTO creationDTO) {
        log.info("Creating new department with name: {}", creationDTO.getName());
        validateDepartmentCreation(creationDTO);

        try {
            if (departmentRepository.existsByName(creationDTO.getName())) {
                throw new DepartmentAlreadyExistsException(
                        "Department with name " + creationDTO.getName() + " already exists",
                        ErrorCode.DEPARTMENT_NAME_EXISTS.getCode()
                );
            }

            Department department = new Department();
            department.setName(creationDTO.getName().trim());
            department.setDescription(creationDTO.getDescription() != null ?
                    creationDTO.getDescription().trim() : null);
            department.setCreatedAt(LocalDateTime.now());
            department.setUpdatedAt(LocalDateTime.now());
            department.setActive(true);

            log.debug("Initial department timestamps - createdAt: {}, updatedAt: {}",
                    department.getCreatedAt(), department.getUpdatedAt());

            Department savedDepartment = departmentRepository.save(department);

            log.info("Department created successfully with ID: {}", savedDepartment.getId());
            return convertToDTO(savedDepartment);

        } catch (DataIntegrityViolationException e) {
            log.error("Database error while creating department", e);
            throw new DepartmentValidationException(
                    "Failed to create department due to data integrity violation",
                    ErrorCode.DATA_INTEGRITY_ERROR.getCode()
            );
        }
    }

    @Cacheable(value = "department", key = "#id")
    public DepartmentDTO getDepartmentById(UUID id) {
        log.info("Fetching department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id,
                        ErrorCode.DEPARTMENT_NOT_FOUND.getCode()
                ));

        if (!department.isActive()) {
            throw new ResourceNotFoundException(
                    "Department with ID: " + id + " is inactive",
                    ErrorCode.DEPARTMENT_INACTIVE.getCode()
            );
        }

        return convertToDTO(department);
    }

    @Cacheable(value = "departments")
    public List<DepartmentDTO> getAllDepartments() {
        log.info("Fetching all active departments");
        List<Department> departments = departmentRepository.findAllByActiveTrue();
        log.debug("Found {} active departments", departments.size());
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "department", key = "#id"),
            @CacheEvict(value = "departments", allEntries = true)
    })
    public DepartmentDTO updateDepartment(UUID id, DepartmentUpdateDTO updateDTO) {
        log.info("Updating department with ID: {}", id);
        validateDepartmentUpdate(updateDTO);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with ID: " + id,
                            ErrorCode.DEPARTMENT_NOT_FOUND.getCode()
                    ));

            if (!department.isActive()) {
                throw new ResourceNotFoundException(
                        "Cannot update inactive department with ID: " + id,
                        ErrorCode.DEPARTMENT_INACTIVE.getCode()
                );
            }

            if (!department.getName().equals(updateDTO.getName()) &&
                    departmentRepository.existsByName(updateDTO.getName())) {
                throw new DepartmentAlreadyExistsException(
                        "Department with name " + updateDTO.getName() + " already exists",
                        ErrorCode.DEPARTMENT_NAME_EXISTS.getCode()
                );
            }

            department.setName(updateDTO.getName().trim());
            department.setDescription(updateDTO.getDescription() != null ?
                    updateDTO.getDescription().trim() : null);
            department.setUpdatedAt(LocalDateTime.now());

            Department updatedDepartment = departmentRepository.save(department);
            log.info("Department updated successfully");

            return convertToDTO(updatedDepartment);

        } catch (DataIntegrityViolationException e) {
            log.error("Database error while updating department", e);
            throw new DepartmentValidationException(
                    "Failed to update department due to data integrity violation",
                    ErrorCode.DATA_INTEGRITY_ERROR.getCode()
            );
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "department", key = "#id"),
            @CacheEvict(value = "departments", allEntries = true)
    })
    public void deleteDepartment(UUID id) {
        log.info("Attempting to delete department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id,
                        ErrorCode.DEPARTMENT_NOT_FOUND.getCode()
                ));

        if (!department.isActive()) {
            throw new ResourceNotFoundException(
                    "Department with ID: " + id + " is already inactive",
                    ErrorCode.DEPARTMENT_INACTIVE.getCode()
            );
        }

        long userCount = userService.getDepartmentUsersCount(department.getId());
        if (userCount > 0) {
            throw new DepartmentNotEmptyException(
                    String.format("Cannot delete department ID %s as it contains %d active users", id, userCount),
                    ErrorCode.DEPARTMENT_HAS_USERS.getCode()
            );
        }

        // Soft delete
        department.setActive(false);
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);

        log.info("Department soft deleted successfully");
    }

    @Caching(evict = {
            @CacheEvict(value = "department", key = "#id"),
            @CacheEvict(value = "departments", allEntries = true)
    })
    public DepartmentDTO reactivateDepartment(UUID id) {
        log.info("Attempting to reactivate department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id,
                        ErrorCode.DEPARTMENT_NOT_FOUND.getCode()
                ));

        if (department.isActive()) {
            throw new DepartmentValidationException(
                    "Department with ID: " + id + " is already active",
                    ErrorCode.DEPARTMENT_ALREADY_ACTIVE.getCode()
            );
        }

        department.setActive(true);
        department.setUpdatedAt(LocalDateTime.now());
        Department reactivatedDepartment = departmentRepository.save(department);

        log.info("Department reactivated successfully");
        return convertToDTO(reactivatedDepartment);
    }

    private void validateDepartmentCreation(DepartmentCreationDTO creationDTO) {
        if (!StringUtils.hasText(creationDTO.getName())) {
            throw new DepartmentValidationException(
                    "Department name cannot be empty",
                    ErrorCode.VALIDATION_ERROR.getCode()
            );
        }

        if (creationDTO.getName().length() > MAX_DEPARTMENT_NAME_LENGTH) {
            throw new DepartmentValidationException(
                    "Department name cannot exceed " + MAX_DEPARTMENT_NAME_LENGTH + " characters",
                    ErrorCode.VALIDATION_ERROR.getCode()
            );
        }

        if (creationDTO.getDescription() != null &&
                creationDTO.getDescription().length() > MAX_DEPARTMENT_DESCRIPTION_LENGTH) {
            throw new DepartmentValidationException(
                    "Department description cannot exceed " + MAX_DEPARTMENT_DESCRIPTION_LENGTH + " characters",
                    ErrorCode.VALIDATION_ERROR.getCode()
            );
        }
    }

    private void validateDepartmentUpdate(DepartmentUpdateDTO updateDTO) {
        if (!StringUtils.hasText(updateDTO.getName())) {
            throw new DepartmentValidationException(
                    "Department name cannot be empty",
                    ErrorCode.VALIDATION_ERROR.getCode()
            );
        }

        if (updateDTO.getName().length() > MAX_DEPARTMENT_NAME_LENGTH) {
            throw new DepartmentValidationException(
                    "Department name cannot exceed " + MAX_DEPARTMENT_NAME_LENGTH + " characters",
                    ErrorCode.VALIDATION_ERROR.getCode()
            );
        }

        if (updateDTO.getDescription() != null &&
                updateDTO.getDescription().length() > MAX_DEPARTMENT_DESCRIPTION_LENGTH) {
            throw new DepartmentValidationException(
                    "Department description cannot exceed " + MAX_DEPARTMENT_DESCRIPTION_LENGTH + " characters",
                    ErrorCode.VALIDATION_ERROR.getCode()
            );
        }
    }

    private DepartmentDTO convertToDTO(Department department) {
        if (department == null) {
            return null;
        }

        long staff = userService.getDepartmentUsersCount(department.getId());

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setStaff(staff);
        dto.setDescription(department.getDescription());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setUpdatedAt(department.getUpdatedAt());
        dto.setActive(department.isActive());

        log.debug("Converting to DTO - Department ID: {}, createdAt: {}, updatedAt: {}, active: {}",
                department.getId(), dto.getCreatedAt(), dto.getUpdatedAt(), dto.isActive());

        return dto;
    }
}