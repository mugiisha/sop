package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping
    public Comment addComment(@RequestBody Comment comment) {
        return commentService.addComment(comment);
    }

    @GetMapping("/stage/{stageId}")
    public List<Comment> getCommentsByStage(@PathVariable String stageId) {
        return commentService.getCommentsByStage(stageId);
    }
}
