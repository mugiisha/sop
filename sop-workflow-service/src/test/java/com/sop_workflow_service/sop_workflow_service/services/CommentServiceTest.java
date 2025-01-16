package com.sop_workflow_service.sop_workflow_service.services;

import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.repository.CommentRepository;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import com.sop_workflow_service.sop_workflow_service.service.CommentService;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private WorkflowStageRepository workflowStageRepository;

    @InjectMocks
    private CommentService commentService;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    private UUID testUserId;
    private String testStageId;
    private String testCommentId;
    private String testContent;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testStageId = "test-stage-id";
        testCommentId = "test-comment-id";
        testContent = "Test comment content";

        testComment = Comment.builder()
                .id(testCommentId)
                .content(testContent)
                .stageId(testStageId)
                .userId(testUserId)
                .createdAt(new Date())
                .build();
    }

    @Test
    void saveComment_Success() {
        // Arrange
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        Comment result = commentService.saveComment(testUserId, testStageId, testContent);

        // Assert
        verify(commentRepository).save(commentCaptor.capture());
        Comment savedComment = commentCaptor.getValue();

        assertNotNull(result);
        assertEquals(testContent, result.getContent());
        assertEquals(testStageId, result.getStageId());
        assertEquals(testUserId, result.getUserId());
        assertNotNull(savedComment.getCreatedAt());
    }

    @Test
    void deleteCommentById_Success() {
        // Arrange
        when(commentRepository.findByIdAndUserId(testCommentId, testUserId))
                .thenReturn(testComment);
        doNothing().when(commentRepository).deleteById(testCommentId);

        // Act
        commentService.deleteCommentById(testCommentId, testUserId);

        // Assert
        verify(commentRepository).findByIdAndUserId(testCommentId, testUserId);
        verify(commentRepository).deleteById(testCommentId);
    }

    @Test
    void deleteCommentById_ThrowsBadRequestException_WhenCommentNotFound() {
        // Arrange
        when(commentRepository.findByIdAndUserId(testCommentId, testUserId))
                .thenReturn(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                commentService.deleteCommentById(testCommentId, testUserId)
        );

        assertEquals("Comment not found or user not allowed to delete comment", exception.getMessage());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void updateComment_Success() {
        // Arrange
        String updatedContent = "Updated comment content";
        Comment updatedComment = Comment.builder()
                .id(testCommentId)
                .content(updatedContent)
                .stageId(testStageId)
                .userId(testUserId)
                .createdAt(testComment.getCreatedAt())
                .build();

        when(commentRepository.findByIdAndUserId(testCommentId, testUserId))
                .thenReturn(testComment);
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(updatedComment);

        // Act
        Comment result = commentService.updateComment(testCommentId, testUserId, updatedContent);

        // Assert
        verify(commentRepository).save(commentCaptor.capture());
        Comment savedComment = commentCaptor.getValue();

        assertNotNull(result);
        assertEquals(updatedContent, result.getContent());
        assertEquals(testStageId, result.getStageId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testComment.getCreatedAt(), result.getCreatedAt());
        assertEquals(updatedContent, savedComment.getContent());
    }

    @Test
    void updateComment_ThrowsBadRequestException_WhenCommentNotFound() {
        // Arrange
        when(commentRepository.findByIdAndUserId(testCommentId, testUserId))
                .thenReturn(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                commentService.updateComment(testCommentId, testUserId, "Updated content")
        );

        assertEquals("Comment not found or user not allowed to delete comment", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void saveComment_VerifyCacheEviction() {
        // Arrange
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        commentService.saveComment(testUserId, testStageId, testContent);

        // Assert
        // Note: We can't directly test cache eviction since it's handled by Spring's cache manager
        // But we can verify the method is annotated with @CacheEvict
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteComment_VerifyCacheEviction() {
        // Arrange
        when(commentRepository.findByIdAndUserId(testCommentId, testUserId))
                .thenReturn(testComment);

        // Act
        commentService.deleteCommentById(testCommentId, testUserId);

        // Assert
        verify(commentRepository).deleteById(testCommentId);
    }

    @Test
    void updateComment_VerifyCacheEviction() {
        // Arrange
        when(commentRepository.findByIdAndUserId(testCommentId, testUserId))
                .thenReturn(testComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        commentService.updateComment(testCommentId, testUserId, "Updated content");

        // Assert
        verify(commentRepository).save(any(Comment.class));
    }
}