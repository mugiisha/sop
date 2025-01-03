package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);


    private final RoleRepository roleRepository;

    private static final String ROLE_NOT_FOUND = "Role not found";

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @CacheEvict(value = "roles", allEntries = true)
    public Role createRole(CreateRoleDto createRoleDto) throws AlreadyExistsException {

            log.info("Creating role");
            String roleName = createRoleDto.getRoleName().toUpperCase();
            Role existingRole = roleRepository.findByRoleName(roleName);

            if(existingRole != null) {
                log.error("Error creating existing role");
                throw new AlreadyExistsException("Role already exists");
            }

            Role role = new Role();
            role.setRoleName(roleName);
            role.setCreatedAt(LocalDateTime.now());
            return roleRepository.save(role);

    }

    @Cacheable(value = "roles", key = "#roleId")
    public Role getRoleById(UUID roleId) throws NotFoundException {

        log.info("Retrieving role with id");

        return roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.error("Error retrieving role with id");
                    return new NotFoundException(ROLE_NOT_FOUND);
                });
    }

    @CacheEvict(value = "roles", allEntries = true)
    public Role updateRole(UUID roleId, CreateRoleDto createRoleDto) throws NotFoundException {

        log.info("Updating role");

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> {
                        log.error("Error role to update not found");
                       return new NotFoundException(ROLE_NOT_FOUND);
                    });

            role.setRoleName(createRoleDto.getRoleName().toUpperCase());
            return roleRepository.save(role);

    }

    @CacheEvict(value = "roles", allEntries = true)
    public void deleteRole(UUID roleId) throws NotFoundException {

        log.info("Deleting role");

            roleRepository.findById(roleId).orElseThrow(
                    () -> {
                        log.error("Error deleting role not found");
                        return new NotFoundException(ROLE_NOT_FOUND);
                    });

            roleRepository.deleteById(roleId);
    }

    @Cacheable(value = "roles")
    public List<Role> getAllRoles() {
             log.info("Retrieving all roles");
            return roleRepository.findAll();
    }

    @Cacheable(value = "roles", key = "#roleName")
    public Role getRoleByRoleName(String roleName) throws NotFoundException {

            log.info("Retrieving role with name");
            Role role = roleRepository.findByRoleName(roleName);

            if(role == null) {
                log.error("Error retrieving role with name not found");
                throw new NotFoundException(ROLE_NOT_FOUND);
            }

            return role;
    }
}
