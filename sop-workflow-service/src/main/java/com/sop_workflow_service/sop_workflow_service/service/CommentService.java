package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.CommentRepository;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final WorkflowStageRepository workflowStageRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, WorkflowStageRepository workflowStageRepository) {
        this.commentRepository = commentRepository;
        this.workflowStageRepository = workflowStageRepository;
   }

    @Caching(evict = {
            @CacheEvict(value = "workflowStage", allEntries = true),
            @CacheEvict(value = "workflowStagesList", allEntries = true)
    })
    public Comment saveComment(UUID userId, String stageId, String comment){
        Comment newComment = Comment.builder()
                .content(comment)
                .stageId(stageId)
                .userId(userId)
                .createdAt(new Date())
                .build();

        return commentRepository.save(newComment);
    }

    @Caching(evict = {
            @CacheEvict(value = "workflowStage", allEntries = true),
            @CacheEvict(value = "workflowStagesList", allEntries = true)
    })
    public void deleteCommentById(String commentId, UUID userId) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId);

        if(comment == null) {
            throw new BadRequestException("Comment not found or user not allowed to delete comment");
        }
        commentRepository.deleteById(commentId);
    }

    @Caching(evict = {
            @CacheEvict(value = "workflowStage", allEntries = true),
            @CacheEvict(value = "workflowStagesList", allEntries = true)
    })
    public Comment updateComment(String commentId,UUID userId, String comment){
        Comment existingComment = commentRepository.findByIdAndUserId(commentId, userId);

        if(existingComment == null) {
            throw new BadRequestException("Comment not found or user not allowed to delete comment");
        }

        existingComment.setContent(comment);
        return commentRepository.save(existingComment);
    }
}
