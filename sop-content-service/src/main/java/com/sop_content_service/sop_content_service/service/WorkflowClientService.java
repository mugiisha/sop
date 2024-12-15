package com.sop_content_service.sop_content_service.service;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import sopWorkflowService.*;

@Service
public class WorkflowClientService {

    @GrpcClient("sop-workflow-service")
    SopWorkflowServiceGrpc.SopWorkflowServiceBlockingStub sopWorkflowServiceBlockingStub;

    public IsSOPApprovedResponse isSOPApproved(String sopId) {
        return sopWorkflowServiceBlockingStub
                .isSOPApproved(
                        IsSOPApprovedRequest
                                .newBuilder()
                                .setId(sopId)
                                .build()
                );
    }
}
