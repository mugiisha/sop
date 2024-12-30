package com.user_management_service.user_management_service.controllers;

import com.user_management_service.user_management_service.dtos.*;
import com.user_management_service.user_management_service.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping("/create")
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentCreationDTO creationDTO) {
        log.info("Received request to create department: {}", creationDTO);
        DepartmentDTO department = departmentService.createDepartment(creationDTO);
        log.info("Created department with timestamps - createdAt: {}, updatedAt: {}",
                department.getCreatedAt(), department.getUpdatedAt());
        return new ResponseEntity<>(department, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable UUID id) {
        log.info("Received request to get department with id: {}", id);
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        log.info("Received request to get all departments");
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentUpdateDTO updateDTO) {
        log.info("Received request to update department with id: {}", id);
        DepartmentDTO department = departmentService.updateDepartment(id, updateDTO);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        log.info("Received request to delete department with id: {}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    // Exception Handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        log.error("An error occurred: ", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}