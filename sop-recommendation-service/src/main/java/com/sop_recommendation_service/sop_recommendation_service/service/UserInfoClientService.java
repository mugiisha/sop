package com.sop_recommendation_service.sop_recommendation_service.service;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import userService.getUserInfoRequest;
import userService.getUserInfoResponse;
import userService.userServiceGrpc;
@Service
@Slf4j
public class UserInfoClientService {
    @GrpcClient("user-management-service")
    userServiceGrpc.userServiceBlockingStub userInfoServiceBlockingStub;

    public getUserInfoResponse getUserInfo(String userId) {
        try {
            log.info("Attempting to fetch user info for userId: {}", userId);
            getUserInfoRequest request = getUserInfoRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            log.info("gRPC Client Stub: {}", userInfoServiceBlockingStub);

            getUserInfoResponse response = userInfoServiceBlockingStub.getUserInfo(request);
            log.info("Received user info response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error in getUserInfo", e);
            throw e;
        }
    }
}