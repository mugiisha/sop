package com.user_management_service.user_management_service.services;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import sopService.*;

@Service
public class UserRoleClientService {

    @GrpcClient("role-access-control-service")
    RoleAssignmentServiceGrpc.RoleAssignmentServiceBlockingStub roleAssignmentServiceBlockingStub;

    public AssignRoleResponse assignRole(String userId, String roleId, String departmentId) {
        AssignRoleRequest request = AssignRoleRequest.newBuilder()
                .setUserId(userId)
                .setRoleId(roleId)
                .setDepartmentId(departmentId)
                .build();

        return roleAssignmentServiceBlockingStub.assignRole(request);
    }

    public GetRoleByUserIdResponse getUserRoles(String userId) {
        return roleAssignmentServiceBlockingStub
                .getRoleAssignmentsByUserId(
                        GetRoleByUserIdRequest
                                .newBuilder()
                                .setUserId(userId)
                                .build()
                );
    }

    public GetRoleAssignmentsByRoleIdAndDepartmentIdResponse getUsersByRoleIdAndDepartmentId(String roleId, String departmentId) {
        return roleAssignmentServiceBlockingStub
                .getRoleAssignmentsByRoleIdAndDepartmentId(
                        GetRoleAssignmentsByRoleIdAndDepartmentIdRequest
                                .newBuilder()
                                .setRoleId(roleId)
                                .setDepartmentId(departmentId)
                                .build()
                );
    }

    public AssignRoleResponse updateRoleAssignment(String userId, String roleId, String departmentId) {
        return roleAssignmentServiceBlockingStub
                .updateUserRole(
                        AssignRoleRequest
                                .newBuilder()
                                .setUserId(userId)
                                .setRoleId(roleId)
                                .setDepartmentId(departmentId)
                                .build()
                );
    }

    public DeleteUserRoleResponse deleteUserRole(String userId) {
        return roleAssignmentServiceBlockingStub
                .deleteUserRole(
                        DeleteUserRoleRequest
                                .newBuilder()
                                .setUserId(userId)
                                .build()
                );
    }

    public GetRoleByNameResponse getRoleByName(String roleName) {
        return roleAssignmentServiceBlockingStub
                .getRoleByName(
                        GetRoleByNameRequest
                                .newBuilder()
                                .setRoleName(roleName)
                                .build()
                );
    }
}