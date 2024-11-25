package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private static final String ROLE_NOT_FOUND = "Role not found";

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(CreateRoleDto createRoleDto) throws AlreadyExistsException {

            String roleName = createRoleDto.getRoleName().toUpperCase();
            Role existingRole = roleRepository.findByRoleName(roleName.toUpperCase());

            if(existingRole != null) {
                throw new AlreadyExistsException("Role already exists");
            }

            Role role = new Role();
            role.setRoleName(createRoleDto.getRoleName());
            return roleRepository.save(role);

    }

    public Role getRoleById(UUID roleId) throws NotFoundException {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND));
    }

    public Role updateRole(UUID roleId, CreateRoleDto createRoleDto) throws NotFoundException {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND));

            role.setRoleName(createRoleDto.getRoleName().toUpperCase());
            return roleRepository.save(role);

    }

    public void deleteRole(UUID roleId) throws NotFoundException {
            roleRepository.findById(roleId).orElseThrow(
                    () -> new NotFoundException(ROLE_NOT_FOUND));

            roleRepository.deleteById(roleId);
    }

    public List<Role> getAllRoles() {
            return roleRepository.findAll();
    }

    public Role getRoleByRoleName(String roleName) throws NotFoundException {
            Role role = roleRepository.findByRoleName(roleName);

            if(role == null) {
                throw new NotFoundException(ROLE_NOT_FOUND);
            }

            return role;
    }
}
