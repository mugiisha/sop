package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import com.role_access_control_service.role_access_control_service.repositories.RoleAssignmentRepository;
import com.role_access_control_service.role_access_control_service.repositories.RoleRepository;
import com.role_access_control_service.role_access_control_service.utils.exception.AlreadyExistsException;
import com.role_access_control_service.role_access_control_service.utils.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoleAssignmentService {

        private final RoleAssignmentRepository roleAssignmentRepository;
        private final RoleRepository roleRepository;

        private static final String ASSIGNMENT_NOT_FOUND = "Role assignment not found";
        private static final String HOD_ASSIGN_ROLE_ERROR = "A department can't have more than one HOD";

        @Autowired
        public RoleAssignmentService(RoleAssignmentRepository roleAssignmentRepository, RoleRepository roleRepository) {
                this.roleAssignmentRepository = roleAssignmentRepository;
                this.roleRepository = roleRepository;
        }

        public RoleAssignment assignRole(AssignRoleDto assignRoleDto) {
                        RoleAssignment roleAssignment = formatValidRoleAssignment(assignRoleDto);
                        return roleAssignmentRepository.save(roleAssignment);
        }

        public RoleAssignment updateRoleAssignment(AssignRoleDto assignRoleDto) throws NotFoundException, AlreadyExistsException {

                        UUID userId = UUID.fromString(assignRoleDto.getUserId());
                        UUID roleId = UUID.fromString(assignRoleDto.getRoleId());
                        UUID departmentId = UUID.fromString(assignRoleDto.getDepartmentId());

                        RoleAssignment existingRoleAssignment = getRoleAssignmentByUserId(userId);

                        if(existingRoleAssignment.getRole().getId().equals(roleId) && existingRoleAssignment.getDepartmentId().equals(departmentId)){
                                throw new AlreadyExistsException("Role assignment already exists");
                        }

                      RoleAssignment roleAssignment = formatValidRoleAssignment(assignRoleDto);

                        return roleAssignmentRepository.save(roleAssignment);
        }


        public RoleAssignment getRoleAssignmentByUserId(UUID userId) throws NotFoundException {
            return roleAssignmentRepository
                    .findByUserId(userId).orElseThrow(() -> new NotFoundException(ASSIGNMENT_NOT_FOUND));
        }

        public void deleteRoleAssignment(UUID userId) {
            RoleAssignment roleAssignment = getRoleAssignmentByUserId(userId);

            roleAssignmentRepository.delete(roleAssignment);
        }

        public List<RoleAssignment> getRoleAssignmentsByRoleIdAndDepartmentId(UUID roleId, UUID departmentId) throws NotFoundException {
                   roleRepository.findById(roleId)
                            .orElseThrow(() -> new NotFoundException("Role not found"));

                   return roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId);
        }

    private Role checkRole(UUID roleId, UUID departmentId) throws NotFoundException, AlreadyExistsException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if ("HOD".equalsIgnoreCase(role.getRoleName())) {
            List<RoleAssignment> existingHodAssignments = roleAssignmentRepository.findByRoleIdAndDepartmentId(roleId, departmentId);
            if (!existingHodAssignments.isEmpty()) {
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