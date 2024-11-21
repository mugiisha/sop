package com.role_access_control_service.role_access_control_service.repositories;

import com.role_access_control_service.role_access_control_service.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByRoleName(String roleName);
}
