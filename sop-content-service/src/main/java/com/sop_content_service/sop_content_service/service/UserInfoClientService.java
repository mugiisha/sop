package com.sop_content_service.sop_content_service.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import userService.*;

@Service
public class UserInfoClientService {

    @GrpcClient("user-management-service")
    userServiceGrpc.userServiceBlockingStub userInfoServiceBlockingStub;


    public getUserInfoResponse getUserInfo(String userId) {
        getUserInfoRequest request = getUserInfoRequest.newBuilder()
                .setUserId(userId)
                .build();

        return userInfoServiceBlockingStub.getUserInfo(request);
    }

    public getDepartmentNameResponse getDepartmentName(String departmentId) {
        getDepartmentNameRequest request = getDepartmentNameRequest.newBuilder()
                .setDepartmentId(departmentId)
                .build();

        return userInfoServiceBlockingStub.getDepartmentName(request);
    }
}
