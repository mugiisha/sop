package com.user_management_service.user_management_service.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DepartmentDTO {
    private UUID id;
    private String name;
    private String description;
    private long staff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}