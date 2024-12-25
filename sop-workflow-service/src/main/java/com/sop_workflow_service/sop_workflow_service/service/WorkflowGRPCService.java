package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sopWorkflowService.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Override
    public void getWorkflowStageInfo(GetWorkflowStageInfoRequest request, StreamObserver<GetWorkflowStageInfoResponse> responseObserver) {
        log.info("Getting workflow stage info");
        try{
            UUID userId = UUID.fromString(request.getUserId());

            WorkflowStage stageInfo = sopService.getStageByUserIdAndSopId(userId, request.getSopId());

            List<Comments> comments = new ArrayList<>();

            if(stageInfo.getComments() != null){
                for(Comment comment : stageInfo.getComments()){
                    if (comment != null) { // Add null check for comment
                        Comments commentDto = Comments.newBuilder()
                                .setCommentId(comment.getId())
                                .setComment(comment.getContent())
                                .setCreatedAt(comment.getCreatedAt().toString())
                                .build();
                        comments.add(commentDto);
                    }
                }

            }

            GetWorkflowStageInfoResponse response = GetWorkflowStageInfoResponse.newBuilder()
                    .setSuccess(true)
                    .setStatus(stageInfo.getApprovalStatus().toString())
                    .addAllComments(comments)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting workflow stage info: ", e);
            GetWorkflowStageInfoResponse response = GetWorkflowStageInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get workflow stage info: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
