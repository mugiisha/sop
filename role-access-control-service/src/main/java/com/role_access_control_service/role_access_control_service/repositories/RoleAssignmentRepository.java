package com.role_access_control_service.role_access_control_service.repositories;

import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
    Optional<RoleAssignment> findByUserId(UUID userId);

    List<RoleAssignment> findByRoleIdAndDepartmentId(UUID roleId, UUID departmentId);
}
