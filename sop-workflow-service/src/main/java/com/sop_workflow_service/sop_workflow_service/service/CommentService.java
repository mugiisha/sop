package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByStage(String stageId) {
        return commentRepository.findByStageId(stageId);
    }
}
