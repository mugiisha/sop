package com.role_access_control_service.role_access_control_service.services;

import com.role_access_control_service.role_access_control_service.dtos.AssignRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.models.RoleAssignment;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sopService.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@GrpcService
public class UserRoleService extends RoleAssignmentServiceGrpc.RoleAssignmentServiceImplBase {

    private final RoleAssignmentService roleAssignmentService;
    private static final Logger log = LoggerFactory.getLogger(UserRoleService.class);
    private final RoleService roleService;


    @Autowired
    public UserRoleService(RoleAssignmentService roleAssignmentService, RoleService roleService) {
        this.roleAssignmentService = roleAssignmentService;
        this.roleService = roleService;
    }


    @Override
    public void getRoleAssignmentsByUserId(GetRoleByUserIdRequest request, StreamObserver<GetRoleByUserIdResponse> responseObserver) {
        try{

            UUID userId = UUID.fromString(request.getUserId());

            RoleAssignment  roleAssignment = roleAssignmentService.getRoleAssignmentByUserId(userId);


            GetRoleByUserIdResponse response = GetRoleByUserIdResponse.newBuilder()
                    .setSuccess(true)
                    .setRoleName(roleAssignment.getRole().getRoleName())
                    .setRoleId(roleAssignment.getRole().getId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch (Exception e){
            log.error("Error getting role assignment by user id: ", e);
            GetRoleByUserIdResponse response = GetRoleByUserIdResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get role assignments by user id: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void assignRole(AssignRoleRequest request, StreamObserver<AssignRoleResponse> responseObserver) {
        try {
            AssignRoleDto assignRoleDto = new AssignRoleDto();
            assignRoleDto.setUserId(request.getUserId());
            assignRoleDto.setRoleId(request.getRoleId());
            assignRoleDto.setDepartmentId(request.getDepartmentId());
            assignRoleDto.setAssignedAt(LocalDateTime.now());

            RoleAssignment roleAssignment = roleAssignmentService.assignRole(assignRoleDto);

            AssignRoleResponse response = AssignRoleResponse.newBuilder()
                    .setSuccess(true)
                    .setAssignmentId(roleAssignment.getId().toString())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error assigning role: ", e);
            AssignRoleResponse response = AssignRoleResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to assign role: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }

    @Override
    public void getRoleAssignmentsByRoleIdAndDepartmentId(GetRoleAssignmentsByRoleIdAndDepartmentIdRequest request, StreamObserver<GetRoleAssignmentsByRoleIdAndDepartmentIdResponse> responseObserver) {
        try {
            UUID roleId = UUID.fromString(request.getRoleId());
            UUID departmentId = UUID.fromString(request.getDepartmentId());

            List<RoleAssignment> roleAssignments = roleAssignmentService.getRoleAssignmentsByRoleIdAndDepartmentId(roleId, departmentId);

            GetRoleAssignmentsByRoleIdAndDepartmentIdResponse response = GetRoleAssignmentsByRoleIdAndDepartmentIdResponse.newBuilder()
                    .setSuccess(true)
                    .addAllUserIds(roleAssignments
                            .stream()
                            .map(roleAssignment -> roleAssignment.getUserId()
                                    .toString())
                            .toList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting role assignments by role id and department id: ", e);
            GetRoleAssignmentsByRoleIdAndDepartmentIdResponse response = GetRoleAssignmentsByRoleIdAndDepartmentIdResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get role assignments by role id and department id: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }


    @Override
    public void updateUserRole(AssignRoleRequest request, StreamObserver<AssignRoleResponse> responseObserver) {
        try {
            AssignRoleDto assignRoleDto = new AssignRoleDto();
            assignRoleDto.setUserId(request.getUserId());
            assignRoleDto.setRoleId(request.getRoleId());
            assignRoleDto.setDepartmentId(request.getDepartmentId());
            assignRoleDto.setAssignedAt(LocalDateTime.now());

            RoleAssignment updatedRoleAssignment = roleAssignmentService.updateRoleAssignment(assignRoleDto);

            AssignRoleResponse response = AssignRoleResponse.newBuilder()
                    .setSuccess(true)
                    .setAssignmentId(updatedRoleAssignment.getId().toString())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating user role: ", e);
            AssignRoleResponse response = AssignRoleResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to update user role: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteUserRole(DeleteUserRoleRequest request, StreamObserver<DeleteUserRoleResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());

            roleAssignmentService.deleteRoleAssignment(userId);

            DeleteUserRoleResponse response = DeleteUserRoleResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting user role: ", e);
            DeleteUserRoleResponse response = DeleteUserRoleResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to delete user role: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getRoleByName(GetRoleByNameRequest request, StreamObserver<GetRoleByNameResponse> responseObserver) {
        try {

          Role role =  roleService.getRoleByRoleName(request.getRoleName());

            GetRoleByNameResponse response = GetRoleByNameResponse.newBuilder()
                    .setSuccess(true)
                    .setRoleName(role.getRoleName())
                    .setRoleId(String.valueOf(role.getId()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting user role: ", e);
            GetRoleByNameResponse response = GetRoleByNameResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get role: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}