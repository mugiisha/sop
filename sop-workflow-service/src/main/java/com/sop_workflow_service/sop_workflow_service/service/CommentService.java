package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.repository.CommentRepository;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public void deleteCommentById(String commentId, UUID userId) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId);

        if(comment == null) {
            throw new BadRequestException("Comment not found or user not allowed to delete comment");
        }
        commentRepository.deleteById(commentId);
    }

    public Comment updateComment(String commentId,UUID userId, String comment){
        Comment existingComment = commentRepository.findByIdAndUserId(commentId, userId);

        if(existingComment == null) {
            throw new BadRequestException("Comment not found or user not allowed to delete comment");
        }

        existingComment.setContent(comment);
        return commentRepository.save(existingComment);
    }
}
