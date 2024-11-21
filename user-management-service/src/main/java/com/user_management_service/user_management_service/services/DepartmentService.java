package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.dtos.DepartmentCreationDTO;
import com.user_management_service.user_management_service.dtos.DepartmentDTO;
import com.user_management_service.user_management_service.dtos.DepartmentUpdateDTO;
import com.user_management_service.user_management_service.exceptions.DepartmentAlreadyExistsException;
import com.user_management_service.user_management_service.repositories.DepartmentRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreationDTO creationDTO) {
        if (departmentRepository.existsByName(creationDTO.getName())) {
            throw new DepartmentAlreadyExistsException("Department with this name already exists");
        }

        Department department = new Department();
        department.setName(creationDTO.getName());
        department.setDescription(creationDTO.getDescription());

        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    public DepartmentDTO getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return convertToDTO(department);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDTO updateDepartment(UUID id, DepartmentUpdateDTO updateDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // Check if new name already exists for a different department
        if (!department.getName().equals(updateDTO.getName()) &&
                departmentRepository.existsByName(updateDTO.getName())) {
            throw new DepartmentAlreadyExistsException("Department with this name already exists");
        }

        department.setName(updateDTO.getName());
        department.setDescription(updateDTO.getDescription());

        Department updatedDepartment = departmentRepository.save(department);
        return convertToDTO(updatedDepartment);
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        return dto;
    }
}