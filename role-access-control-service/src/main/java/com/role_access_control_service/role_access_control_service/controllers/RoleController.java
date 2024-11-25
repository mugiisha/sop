package com.role_access_control_service.role_access_control_service.controllers;

import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.services.RoleService;
import com.role_access_control_service.role_access_control_service.utils.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public Response<Role> createRole(@RequestBody CreateRoleDto createRoleDto) {
        Role role =  roleService.createRole(createRoleDto);
        return new Response<>(true, "Role created successfully", role);
    }

    @GetMapping("/{roleId}")
    public Response<Role> getRoleById(@PathVariable UUID roleId) {
        Role role = roleService.getRoleById(roleId);
        return new Response<>(true, "Role retrieved successfully", role);
    }

    @PutMapping("/{roleId}")
    public Response<Role> updateRole(@PathVariable UUID roleId, @RequestBody CreateRoleDto createRoleDto) {
        Role role = roleService.updateRole(roleId, createRoleDto);
        return new Response<>(true, "Role updated successfully", role);
    }

    @DeleteMapping("/{roleId}")
    public Response<String> deleteRole(@PathVariable UUID roleId) throws Exception {
        roleService.deleteRole(roleId);
        return new Response<>(true, "Role deleted successfully", null);
    }

    @GetMapping
    public Response<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return new Response<>(true, "Roles retrieved successfully", roles);
    }
}
