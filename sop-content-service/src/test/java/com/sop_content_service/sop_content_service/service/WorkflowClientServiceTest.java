package com.sop_content_service.sop_content_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopWorkflowService.*;
import sopWorkflowService.SopWorkflowServiceGrpc.SopWorkflowServiceBlockingStub;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkflowClientServiceTest {

    @Mock
    private SopWorkflowServiceBlockingStub sopWorkflowServiceBlockingStub;

    @InjectMocks
    private WorkflowClientService workflowClientService;

    private static final String TEST_SOP_ID = "sop123";
    private static final String TEST_USER_ID = "user456";
    private static final String TEST_COMMENT_ID = "comment789";
    private static final String TEST_STATUS = "IN_REVIEW";

    @BeforeEach
    void setUp() {
        // Method intentionally left empty as mocks are handled by annotations
    }

    @Test
    void isSOPApproved_ShouldReturnApprovedResponse() {
        // Arrange
        IsSOPApprovedResponse expectedResponse = IsSOPApprovedResponse.newBuilder()
                .setSuccess(true)
                .setSOPApproved(true)
                .build();

        when(sopWorkflowServiceBlockingStub.isSOPApproved(any(IsSOPApprovedRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        IsSOPApprovedResponse actualResponse = workflowClientService.isSOPApproved(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertTrue(actualResponse.getSOPApproved());
    }

    @Test
    void isSOPApproved_ShouldReturnNotApprovedResponse() {
        // Arrange
        IsSOPApprovedResponse expectedResponse = IsSOPApprovedResponse.newBuilder()
                .setSuccess(true)
                .setSOPApproved(false)
                .build();

        when(sopWorkflowServiceBlockingStub.isSOPApproved(any(IsSOPApprovedRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        IsSOPApprovedResponse actualResponse = workflowClientService.isSOPApproved(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertFalse(actualResponse.getSOPApproved());
    }

    @Test
    void isSOPApproved_ShouldHandleErrorResponse() {
        // Arrange
        String errorMessage = "SOP not found";
        IsSOPApprovedResponse errorResponse = IsSOPApprovedResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(errorMessage)
                .build();

        when(sopWorkflowServiceBlockingStub.isSOPApproved(any(IsSOPApprovedRequest.class)))
                .thenReturn(errorResponse);

        // Act
        IsSOPApprovedResponse actualResponse = workflowClientService.isSOPApproved(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.getSuccess());
        assertEquals(errorMessage, actualResponse.getErrorMessage());
    }

    @Test
    void getWorkflowStage_ShouldReturnValidResponse() {
        // Arrange
        Comments comment1 = Comments.newBuilder()
                .setCommentId(TEST_COMMENT_ID)
                .setComment("Great work!")
                .setCreatedAt("2024-01-15T10:00:00Z")
                .build();

        Comments comment2 = Comments.newBuilder()
                .setCommentId(TEST_COMMENT_ID + "2")
                .setComment("Please review")
                .setCreatedAt("2024-01-15T11:00:00Z")
                .build();

        GetWorkflowStageInfoResponse expectedResponse = GetWorkflowStageInfoResponse.newBuilder()
                .setSuccess(true)
                .setStatus(TEST_STATUS)
                .addAllComments(Arrays.asList(comment1, comment2))
                .build();

        when(sopWorkflowServiceBlockingStub.getWorkflowStageInfo(any(GetWorkflowStageInfoRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        GetWorkflowStageInfoResponse actualResponse = workflowClientService.getWorkflowStage(TEST_USER_ID, TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(TEST_STATUS, actualResponse.getStatus());
        assertEquals(2, actualResponse.getCommentsCount());

        // Verify first comment
        Comments firstComment = actualResponse.getComments(0);
        assertEquals(TEST_COMMENT_ID, firstComment.getCommentId());
        assertEquals("Great work!", firstComment.getComment());
        assertEquals("2024-01-15T10:00:00Z", firstComment.getCreatedAt());
    }

    @Test
    void getWorkflowStage_ShouldHandleEmptyComments() {
        // Arrange
        GetWorkflowStageInfoResponse expectedResponse = GetWorkflowStageInfoResponse.newBuilder()
                .setSuccess(true)
                .setStatus(TEST_STATUS)
                .build();

        when(sopWorkflowServiceBlockingStub.getWorkflowStageInfo(any(GetWorkflowStageInfoRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        GetWorkflowStageInfoResponse actualResponse = workflowClientService.getWorkflowStage(TEST_USER_ID, TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(TEST_STATUS, actualResponse.getStatus());
        assertEquals(0, actualResponse.getCommentsCount());
        assertTrue(actualResponse.getCommentsList().isEmpty());
    }

    @Test
    void getWorkflowStage_ShouldHandleErrorResponse() {
        // Arrange
        String errorMessage = "Invalid user or SOP";
        GetWorkflowStageInfoResponse errorResponse = GetWorkflowStageInfoResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(errorMessage)
                .build();

        when(sopWorkflowServiceBlockingStub.getWorkflowStageInfo(any(GetWorkflowStageInfoRequest.class)))
                .thenReturn(errorResponse);

        // Act
        GetWorkflowStageInfoResponse actualResponse = workflowClientService.getWorkflowStage(TEST_USER_ID, TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.getSuccess());
        assertEquals(errorMessage, actualResponse.getErrorMessage());
        assertTrue(actualResponse.getCommentsList().isEmpty());
    }
}