package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import com.role_access_control_service.role_access_control_service.repositories.RoleAssignmentRepository;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoleAssignmentService {

        private final RoleAssignmentRepository roleAssignmentRepository;
        private final RoleRepository roleRepository;
        private static final Logger log = LoggerFactory.getLogger(RoleAssignmentService.class);


    private static final String ASSIGNMENT_NOT_FOUND = "Role assignment not found";
        private static final String HOD_ASSIGN_ROLE_ERROR = "A department can't have more than one HOD";

        @Autowired
        public RoleAssignmentService(RoleAssignmentRepository roleAssignmentRepository, RoleRepository roleRepository) {
                this.roleAssignmentRepository = roleAssignmentRepository;
                this.roleRepository = roleRepository;
        }

        @CacheEvict(value = "roleAssignments", allEntries = true)
        public RoleAssignment assignRole(AssignRoleDto assignRoleDto) {
                        log.info("Assigning role to user");
                        RoleAssignment roleAssignment = formatValidRoleAssignment(assignRoleDto);
                        return roleAssignmentRepository.save(roleAssignment);
        }

        @Caching(
                put = {
                        @CachePut(value = "roleAssignments", key = "#assignRoleDto.userId"),
                        @CachePut(value = "roleAssignments", key = "{#assignRoleDto.roleId, #assignRoleDto.departmentId}")
                }
        )
        public RoleAssignment updateRoleAssignment(AssignRoleDto assignRoleDto) throws NotFoundException, AlreadyExistsException {

                        log.info("Updating role assignment");

                        UUID userId = UUID.fromString(assignRoleDto.getUserId());
                        UUID roleId = UUID.fromString(assignRoleDto.getRoleId());
                        UUID departmentId = UUID.fromString(assignRoleDto.getDepartmentId());

                        RoleAssignment existingRoleAssignment = getUserRoles(userId);

                        if(existingRoleAssignment.getRole().getId().equals(roleId) && existingRoleAssignment.getDepartmentId().equals(departmentId)){
                                return existingRoleAssignment;
                        }

                      RoleAssignment roleAssignment = formatValidRoleAssignment(assignRoleDto);

                        return roleAssignmentRepository.save(roleAssignment);
        }

        @Cacheable(value = "roleAssignments", key = "#userId")
        public RoleAssignment getUserRoles(UUID userId) throws NotFoundException {

            log.info("Retrieving role by user id");
            return roleAssignmentRepository
                    .findByUserId(userId).orElseThrow(() -> new NotFoundException(ASSIGNMENT_NOT_FOUND));
        }

        @CacheEvict(value = "roleAssignments", allEntries = true)
        public void deleteRoleAssignment(UUID userId) {
            log.info("Deleting user role");
            RoleAssignment roleAssignment = getUserRoles(userId);

            roleAssignmentRepository.delete(roleAssignment);
        }

        @Cacheable(value = "roleAssignments",  key = "{#roleId, #departmentId}")
        public List<RoleAssignment> getRoleAssignmentsByRoleIdAndDepartmentId(UUID roleId, UUID departmentId) throws NotFoundException {
                  log.info("getting user roles by role id and department id");
                   roleRepository.findById(roleId)
                            .orElseThrow(() -> {
                                log.error("Error retrieving role with id: {}", roleId);
                               return new NotFoundException("Role not found");
                            });

                   return roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId);
        }

    public boolean checkAssignmentExistsByRoleId(UUID roleId) {
        log.info("Check if role assignments exist by role id");
        return roleAssignmentRepository.existsByRoleId(roleId);
    }


    private Role checkRole(UUID roleId, UUID departmentId) throws NotFoundException, AlreadyExistsException {
            log.info("Checking role");
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.info("Error retrieving role with id: {}", roleId);
                    return new NotFoundException("Role not found");
                });

        if ("HOD".equalsIgnoreCase(role.getRoleName())) {
            List<RoleAssignment> existingHodAssignments = roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId);
            if (!existingHodAssignments.isEmpty()) {
                log.info("Error assigning 2 HODs to the same department");
                throw new AlreadyExistsException(HOD_ASSIGN_ROLE_ERROR);
            }
        }

        return role;
    }

    private RoleAssignment formatValidRoleAssignment(AssignRoleDto assignRoleDto) {
        UUID userId = UUID.fromString(assignRoleDto.getUserId());
        UUID roleId = UUID.fromString(assignRoleDto.getRoleId());
        UUID departmentId = UUID.fromString(assignRoleDto.getDepartmentId());

        Role role = checkRole(roleId, departmentId);

        return RoleAssignment.builder()
                .userId(userId)
                .role(role)
                .departmentId(departmentId)
                .assignedAt(LocalDateTime.now())
                .build();
    }


}