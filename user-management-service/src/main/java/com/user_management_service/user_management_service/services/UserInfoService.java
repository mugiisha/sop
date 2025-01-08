package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.DepartmentDTO;
import com.user_management_service.user_management_service.dtos.UserResponseDTO;
import com.user_management_service.user_management_service.models.User;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import userService.*;

import java.util.UUID;

@Slf4j
@GrpcService
public class UserInfoService extends userServiceGrpc.userServiceImplBase {
    private final UserService userService;
    private final DepartmentService departmentService;

    @Autowired
    public UserInfoService(UserService userService, DepartmentService departmentService) {
        this.userService = userService;
        this.departmentService = departmentService;
    }

    @Override
    public void getUserInfo(getUserInfoRequest request, StreamObserver<getUserInfoResponse> responseObserver) {
        try {
            log.info("Fetching user info by user id");
            UUID userId = UUID.fromString(request.getUserId());

            UserResponseDTO user = userService.getUserById(userId);

            getUserInfoResponse response = getUserInfoResponse.newBuilder()
                    .setSuccess(true)
                    .setName(user.getName())
                    .setProfilePictureUrl(user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : "")
                    .setDepartmentName(user.getDepartmentName())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error fetching user info by user id: ", e);
            getUserInfoResponse response = getUserInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get user info by user id: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getDepartmentName(getDepartmentNameRequest request, StreamObserver<getDepartmentNameResponse> responseObserver) {
        try {
            log.info("Fetching department name by id");

            UUID departmentId = UUID.fromString(request.getDepartmentId());

            DepartmentDTO department = departmentService.getDepartmentById(departmentId);

            getDepartmentNameResponse response = getDepartmentNameResponse.newBuilder()
                    .setSuccess(true)
                    .setDepartmentName(department.getName())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error fetching department name by id: ", e);
            getDepartmentNameResponse response = getDepartmentNameResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to fetch department name by id: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
