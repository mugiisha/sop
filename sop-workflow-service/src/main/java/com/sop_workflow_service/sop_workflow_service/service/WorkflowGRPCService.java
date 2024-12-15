package com.sop_workflow_service.sop_workflow_service.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sopWorkflowService.*;

@Slf4j
@GrpcService
public class WorkflowGRPCService extends SopWorkflowServiceGrpc.SopWorkflowServiceImplBase {

    private final SOPService sopService;

    @Autowired
    public WorkflowGRPCService(SOPService sopService) {
        this.sopService = sopService;
    }

    @Override
    public void isSOPApproved(IsSOPApprovedRequest request, StreamObserver<IsSOPApprovedResponse> responseObserver) {
        try {
            log.info("Checking if SOP is approved");
            String sopId = request.getId();
            boolean isApproved = sopService.isSopApproved(sopId);

            IsSOPApprovedResponse response = IsSOPApprovedResponse.newBuilder()
                    .setSuccess(true)
                    .setSOPApproved(isApproved)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error checking if SOP is approved: ", e);
            IsSOPApprovedResponse response = IsSOPApprovedResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to check if SOP is approved: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
