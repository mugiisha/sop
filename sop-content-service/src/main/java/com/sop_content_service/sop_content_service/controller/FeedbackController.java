package com.sop_content_service.sop_content_service.controller;

import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.model.FeedbackModel;
import com.sop_content_service.sop_content_service.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * Create a new feedback.
     * @param sopId SOP ID.
     * @param userId User ID.
     * @param feedbackModel Feedback data.
     * @return Created feedback response.
     */
    @PostMapping("/{sopId}/{userId}")
    public ResponseEntity<ApiResponse<FeedbackModel>> createFeedback(
            @PathVariable String sopId,
            @PathVariable String userId,
            @RequestBody FeedbackModel feedbackModel
    ) {
        return feedbackService.createFeedback(sopId, userId, feedbackModel);
    }

    /**
     * Get all feedbacks for a specific SOP.
     * @param sopId SOP ID.
     * @return List of feedbacks for the specified SOP.
     */
    @GetMapping("/sop/{sopId}")
    public ResponseEntity<ApiResponse<List<FeedbackModel>>> getFeedbacksBySopId(
            @PathVariable String sopId
    ) {
        return feedbackService.getFeedbacksBySopId(sopId);
    }

    /**
     * Get feedback by its ID.
     * @param feedbackId Feedback ID.
     * @return Feedback with the specified ID.
     */
    @GetMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackModel>> getFeedbackById(
            @PathVariable String feedbackId
    ) {
        return feedbackService.getFeedbackById(feedbackId);
    }

    /**
     * Get all feedbacks for a specific user.
     * @param userId User ID.
     * @return List of feedbacks created by the user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<FeedbackModel>>> getFeedbacksByUserId(
            @PathVariable String userId
    ) {
        return feedbackService.getFeedbacksByUserId(userId);
    }
}
