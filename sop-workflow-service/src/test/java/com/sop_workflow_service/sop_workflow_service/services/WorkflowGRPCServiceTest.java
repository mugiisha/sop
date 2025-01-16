package com.sop_workflow_service.sop_workflow_service.services;

import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.service.SOPService;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowGRPCService;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowStageService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sopWorkflowService.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WorkflowGRPCServiceTest {

    @Mock
    private SOPService sopService;

    @Mock
    private WorkflowStageService workflowStageService;

    @Mock
    private StreamObserver<IsSOPApprovedResponse> isSOPApprovedResponseObserver;

    @Mock
    private StreamObserver<GetWorkflowStageInfoResponse> getWorkflowStageInfoResponseObserver;

    private WorkflowGRPCService workflowGRPCService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workflowGRPCService = new WorkflowGRPCService(sopService, workflowStageService);
    }

    @Test
    void isSOPApproved_Success() {
        // Arrange
        String sopId = "test-sop-id";
        IsSOPApprovedRequest request = IsSOPApprovedRequest.newBuilder()
                .setId(sopId)
                .build();

        when(workflowStageService.isSopApproved(sopId)).thenReturn(true);

        // Act
        workflowGRPCService.isSOPApproved(request, isSOPApprovedResponseObserver);

        // Assert
        verify(workflowStageService).isSopApproved(sopId);
        verify(isSOPApprovedResponseObserver).onNext(argThat(response ->
                response.getSuccess() && response.getSOPApproved()
        ));
        verify(isSOPApprovedResponseObserver).onCompleted();
    }

    @Test
    void isSOPApproved_HandleException() {
        // Arrange
        String sopId = "test-sop-id";
        IsSOPApprovedRequest request = IsSOPApprovedRequest.newBuilder()
                .setId(sopId)
                .build();

        when(workflowStageService.isSopApproved(sopId))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        workflowGRPCService.isSOPApproved(request, isSOPApprovedResponseObserver);

        // Assert
        verify(isSOPApprovedResponseObserver).onNext(argThat(response ->
                !response.getSuccess() &&
                        response.getErrorMessage().contains("Failed to check if SOP is approved")
        ));
        verify(isSOPApprovedResponseObserver).onCompleted();
    }

    @Test
    void getWorkflowStageInfo_Success() {
        // Arrange
        String sopId = "test-sop-id";
        String userId = UUID.randomUUID().toString();
        GetWorkflowStageInfoRequest request = GetWorkflowStageInfoRequest.newBuilder()
                .setSopId(sopId)
                .setUserId(userId)
                .build();

        Comment comment = new Comment();
        comment.setId("comment-id");
        comment.setContent("Test comment");
        // Convert LocalDateTime to Date
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        comment.setCreatedAt(date);

        WorkflowStage workflowStage = new WorkflowStage();
        workflowStage.setApprovalStatus(ApprovalStatus.valueOf("PENDING"));
        workflowStage.setComments(Arrays.asList(comment));

        when(workflowStageService.getStageBySopIdAndUserId(eq(sopId), any(UUID.class)))
                .thenReturn(workflowStage);

        // Act
        workflowGRPCService.getWorkflowStageInfo(request, getWorkflowStageInfoResponseObserver);

        // Assert
        verify(workflowStageService).getStageBySopIdAndUserId(eq(sopId), any(UUID.class));
        verify(getWorkflowStageInfoResponseObserver).onNext(argThat(response ->
                response.getSuccess() &&
                        response.getStatus().equals("PENDING") &&
                        response.getCommentsList().size() == 1 &&
                        response.getCommentsList().get(0).getCommentId().equals("comment-id")
        ));
        verify(getWorkflowStageInfoResponseObserver).onCompleted();
    }

    @Test
    void getWorkflowStageInfo_HandleException() {
        // Arrange
        String sopId = "test-sop-id";
        String userId = UUID.randomUUID().toString();
        GetWorkflowStageInfoRequest request = GetWorkflowStageInfoRequest.newBuilder()
                .setSopId(sopId)
                .setUserId(userId)
                .build();

        when(workflowStageService.getStageBySopIdAndUserId(eq(sopId), any(UUID.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        workflowGRPCService.getWorkflowStageInfo(request, getWorkflowStageInfoResponseObserver);

        // Assert
        verify(getWorkflowStageInfoResponseObserver).onNext(argThat(response ->
                !response.getSuccess() &&
                        response.getErrorMessage().contains("Failed to get workflow stage info")
        ));
        verify(getWorkflowStageInfoResponseObserver).onCompleted();
    }

    @Test
    void getWorkflowStageInfo_HandleNullComments() {
        // Arrange
        String sopId = "test-sop-id";
        String userId = UUID.randomUUID().toString();
        GetWorkflowStageInfoRequest request = GetWorkflowStageInfoRequest.newBuilder()
                .setSopId(sopId)
                .setUserId(userId)
                .build();

        WorkflowStage workflowStage = new WorkflowStage();
        workflowStage.setApprovalStatus(ApprovalStatus.valueOf("PENDING"));
        workflowStage.setComments(null);

        when(workflowStageService.getStageBySopIdAndUserId(eq(sopId), any(UUID.class)))
                .thenReturn(workflowStage);

        // Act
        workflowGRPCService.getWorkflowStageInfo(request, getWorkflowStageInfoResponseObserver);

        // Assert
        verify(workflowStageService).getStageBySopIdAndUserId(eq(sopId), any(UUID.class));
        verify(getWorkflowStageInfoResponseObserver).onNext(argThat(response ->
                response.getSuccess() &&
                        response.getStatus().equals("PENDING") &&
                        response.getCommentsList().isEmpty()
        ));
        verify(getWorkflowStageInfoResponseObserver).onCompleted();
    }
}