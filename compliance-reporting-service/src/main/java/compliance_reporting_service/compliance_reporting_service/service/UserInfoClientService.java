package compliance_reporting_service.compliance_reporting_service.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import userService.getUserInfoRequest;
import userService.getUserInfoResponse;
import userService.userServiceGrpc;

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
}
