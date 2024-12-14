package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * Create a new feedback.
     * @param sopId SOP ID.
     * @param feedbackModel Feedback data.
     * @return Created feedback response.
     */
    @PostMapping("/{sopId}")
    public ResponseEntity<ApiResponse<FeedbackModel>> createFeedback(
            HttpServletRequest request,
            @PathVariable String sopId,
            @RequestBody FeedbackModel feedbackModel
    ) {
        // Retrieve user ID from the request header
        String userId = request.getHeader("X-User-Id");
        // Check if the user ID header is missing
        if (userId == null || userId.isEmpty()) {
            ApiResponse<FeedbackModel> response = new ApiResponse<>("User ID header is missing", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
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
