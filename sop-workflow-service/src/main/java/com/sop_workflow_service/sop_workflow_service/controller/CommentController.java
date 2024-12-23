package com.sop_workflow_service.sop_workflow_service.controller;

import com.sop_workflow_service.sop_workflow_service.dto.CommentDto;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.service.CommentService;
import com.sop_workflow_service.sop_workflow_service.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/comments")
public class CommentController {

    private final CommentService  commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PutMapping("/{commentId}")
    public Response<Object> updateComment(HttpServletRequest request,
                                          @PathVariable String commentId,
                                          @Valid @RequestBody CommentDto commentDto) {

        String userId = request.getHeader("X-User-Id");
        Comment comment = commentService.updateComment(commentId, UUID.fromString(userId), commentDto.getComment());

        return new Response<>(true, "Comment updated successfully", comment);
    }

    @DeleteMapping("/{commentId}")
    public Response<Object> deleteComment(HttpServletRequest request,
                                          @PathVariable String commentId) {

        String userId = request.getHeader("X-User-Id");
        commentService.deleteCommentById(commentId, UUID.fromString(userId));

        return new Response<>(true, "Comment deleted successfully", null);
    }
}
