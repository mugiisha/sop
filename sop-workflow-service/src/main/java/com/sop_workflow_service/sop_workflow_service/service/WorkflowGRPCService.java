package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.dto.SopByStatusDto;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
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
    private final WorkflowStageService workflowStageService;

    @Autowired
    public WorkflowGRPCService(SOPService sopService, WorkflowStageService workflowStageService) {
        this.sopService = sopService;
        this.workflowStageService = workflowStageService;
    }

    @Override
    public void isSOPApproved(IsSOPApprovedRequest request, StreamObserver<IsSOPApprovedResponse> responseObserver) {
        try {
            log.info("Checking if SOP is approved");
            String sopId = request.getId();
            boolean isApproved = workflowStageService.isSopApproved(sopId);

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

            WorkflowStage stageInfo = workflowStageService.getStageBySopIdAndUserId( request.getSopId(),userId);

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

    @Override
    public void getDepartmentSopsByStatus(GetDepartmentSopsByStatusRequest request, StreamObserver<GetDepartmentSopsByStatusResponse> responseObserver) {
        log.info("Getting department SOPs by status");
        try{
            UUID departmentId = UUID.fromString(request.getDepartmentId());

            List<SopByStatusDto> sopsByStatus = sopService.getSopsByDepartmentId( departmentId);

            List<SopByStatus> sops = new ArrayList<>();

            for(SopByStatusDto sop : sopsByStatus){
                    if (sop != null) { // Add null check for sop
                        SopByStatus sopByStatus = SopByStatus.newBuilder()
                                .setId(sop.getId())
                                .setTitle(sop.getTitle())
                                .setStatus(sop.getStatus().toString())
                                .setCreatedAt(sop.getCreatedAt().toString())
                                .setUpdatedAt(sop.getUpdatedAt().toString())
                                .build();
                        sops.add(sopByStatus);
                    }
                }

            GetDepartmentSopsByStatusResponse response = GetDepartmentSopsByStatusResponse.newBuilder()
                    .setSuccess(true)
                    .addAllSops(sops)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting workflow stage info: ", e);
            GetDepartmentSopsByStatusResponse response = GetDepartmentSopsByStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get sops by department id and status: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSopsByStatus(GetSopsStatusRequest request, StreamObserver<GetDepartmentSopsByStatusResponse> responseObserver) {
        log.info("Getting SOPs by status");
        try{
            List<SopByStatusDto> sopsByStatus = sopService.getSops();

            List<SopByStatus> sops = new ArrayList<>();

            for(SopByStatusDto sop : sopsByStatus){
                    if (sop != null) { // Add null check for sop
                        SopByStatus sopByStatus = SopByStatus.newBuilder()
                                .setId(sop.getId())
                                .setTitle(sop.getTitle())
                                .setStatus(sop.getStatus().toString())
                                .setCreatedAt(sop.getCreatedAt().toString())
                                .setUpdatedAt(sop.getUpdatedAt().toString())
                                .build();
                        sops.add(sopByStatus);
                    }
                }

            GetDepartmentSopsByStatusResponse response = GetDepartmentSopsByStatusResponse.newBuilder()
                    .setSuccess(true)
                    .addAllSops(sops)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting workflow stage info: ", e);
            GetDepartmentSopsByStatusResponse response = GetDepartmentSopsByStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to get sops by status: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
