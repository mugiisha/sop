package com.sop_workflow_service.sop_workflow_service.services;

import com.sop_workflow_service.sop_workflow_service.dto.UpdateStageDto;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import com.sop_workflow_service.sop_workflow_service.service.CommentService;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowStageService;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowStageServiceTest {

    @Mock
    private WorkflowStageRepository workflowStageRepository;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private WorkflowStageService workflowStageService;

    private String sopId;
    private UUID userId;
    private WorkflowStage workflowStage;

    @BeforeEach
    void setUp() {
        sopId = "test-sop-123";
        userId = UUID.randomUUID();
        workflowStage = new WorkflowStage();
        workflowStage.setId("stage-123");
        workflowStage.setSopId(sopId);
        workflowStage.setUserId(userId);
        workflowStage.setApprovalStatus(ApprovalStatus.PENDING);
    }

    @Test
    void getStagesBySopId_Success() {
        // Arrange
        List<WorkflowStage> expectedStages = Arrays.asList(workflowStage);
        when(workflowStageRepository.findBySopId(sopId)).thenReturn(expectedStages);

        // Act
        List<WorkflowStage> actualStages = workflowStageService.getStagesBySopId(sopId);

        // Assert
        assertNotNull(actualStages);
        assertEquals(expectedStages.size(), actualStages.size());
        verify(workflowStageRepository).findBySopId(sopId);
    }

    @Test
    void getStageBySopIdAndUserId_Success() {
        // Arrange
        when(workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId))
                .thenReturn(Optional.of(workflowStage));

        // Act
        WorkflowStage result = workflowStageService.getStageBySopIdAndUserId(sopId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(workflowStage.getId(), result.getId());
        assertEquals(workflowStage.getSopId(), result.getSopId());
        assertEquals(workflowStage.getUserId(), result.getUserId());
    }

    @Test
    void getStageBySopIdAndUserId_ThrowsNotFoundException() {
        // Arrange
        when(workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                workflowStageService.getStageBySopIdAndUserId(sopId, userId));
    }

    @Test
    void updateStage_Success() {
        // Arrange
        UpdateStageDto updateStageDto = UpdateStageDto.builder()
                .approvalStatus(ApprovalStatus.APPROVED)
                .comment("Test comment")
                .build();

        Comment newComment = new Comment();
        newComment.setId("comment-123");
        newComment.setContent(updateStageDto.getComment());

        when(workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId))
                .thenReturn(Optional.of(workflowStage));
        when(commentService.saveComment(any(), any(), any())).thenReturn(newComment);
        when(workflowStageRepository.save(any(WorkflowStage.class))).thenReturn(workflowStage);

        // Act
        WorkflowStage result = workflowStageService.updateStage(userId, sopId, updateStageDto);

        // Assert
        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());
        verify(commentService).saveComment(eq(userId), eq(workflowStage.getId()), eq(updateStageDto.getComment()));
        verify(workflowStageRepository).save(any(WorkflowStage.class));
    }

    @Test
    void updateStage_ThrowsBadRequestException_WhenRevisionWithoutComment() {
        // Arrange
        UpdateStageDto updateStageDto = UpdateStageDto.builder()
                .approvalStatus(ApprovalStatus.REVISION)
                .comment(null)
                .build();

        when(workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId))
                .thenReturn(Optional.of(workflowStage));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                workflowStageService.updateStage(userId, sopId, updateStageDto));
    }

    @Test
    void isSopApproved_ReturnsTrue_WhenAllStagesApproved() {
        // Arrange
        WorkflowStage stage1 = new WorkflowStage();
        stage1.setApprovalStatus(ApprovalStatus.APPROVED);
        WorkflowStage stage2 = new WorkflowStage();
        stage2.setApprovalStatus(ApprovalStatus.APPROVED);
        List<WorkflowStage> approvedStages = Arrays.asList(stage1, stage2);

        when(workflowStageRepository.findBySopId(sopId)).thenReturn(approvedStages);

        // Act
        boolean result = workflowStageService.isSopApproved(sopId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isSopApproved_ReturnsFalse_WhenNotAllStagesApproved() {
        // Arrange
        WorkflowStage stage1 = new WorkflowStage();
        stage1.setApprovalStatus(ApprovalStatus.APPROVED);
        WorkflowStage stage2 = new WorkflowStage();
        stage2.setApprovalStatus(ApprovalStatus.PENDING);
        List<WorkflowStage> mixedStages = Arrays.asList(stage1, stage2);

        when(workflowStageRepository.findBySopId(sopId)).thenReturn(mixedStages);

        // Act
        boolean result = workflowStageService.isSopApproved(sopId);

        // Assert
        assertFalse(result);
    }

    @Test
    void saveStages_Success() {
        // Arrange
        List<WorkflowStage> stages = Arrays.asList(workflowStage);
        when(workflowStageRepository.saveAll(stages)).thenReturn(stages);

        // Act
        List<WorkflowStage> result = workflowStageService.saveStages(stages);

        // Assert
        assertNotNull(result);
        assertEquals(stages.size(), result.size());
        verify(workflowStageRepository).saveAll(stages);
    }

    @Test
    void saveStage_Success() {
        // Arrange
        when(workflowStageRepository.save(workflowStage)).thenReturn(workflowStage);

        // Act
        WorkflowStage result = workflowStageService.saveStage(workflowStage);

        // Assert
        assertNotNull(result);
        assertEquals(workflowStage.getId(), result.getId());
        verify(workflowStageRepository).save(workflowStage);
    }
}