package com.analytics_insights_service.analytics_insights_service.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import sopWorkflowService.*;

import java.util.UUID;


@Service
public class SopWorkflowClientService {

    @GrpcClient("sop-workflow-service")
    SopWorkflowServiceGrpc.SopWorkflowServiceBlockingStub workflowServiceBlockingStub;

    public GetDepartmentSopsByStatusResponse getDepartmentSopsByStatus(UUID departmentId) {
        GetDepartmentSopsByStatusRequest request = GetDepartmentSopsByStatusRequest.newBuilder()
                .setDepartmentId(departmentId.toString())
                .build();

        return workflowServiceBlockingStub.getDepartmentSopsByStatus(request);
    }

    public GetDepartmentSopsByStatusResponse getSopsByStatusRequest(){
        GetSopsStatusRequest request = GetSopsStatusRequest.newBuilder()
                .build();

        return workflowServiceBlockingStub.getSopsByStatus(request);
    }
}
