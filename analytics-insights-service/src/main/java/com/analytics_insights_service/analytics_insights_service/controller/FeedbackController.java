package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    /**
     * Create a new feedback.
     * @param sopId SOP ID.
     * @param feedbackModel Feedback data.
     * @return Created feedback response.
     */
    // Create feedback based on sopId
    @PutMapping("/{sopId}")
    public ResponseEntity<ApiResponse<FeedbackModel>> createFeedback(
            @PathVariable String sopId,
            @RequestBody FeedbackModel feedbackModel,
            HttpServletRequest request) {
        return feedbackService.createFeedback(sopId, feedbackModel, request);
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
     * Delete feedback by feedback ID (Only HOD can delete)
     */
    @DeleteMapping("/delete/{feedbackId}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable String feedbackId, HttpServletRequest request) {
        return feedbackService.deleteFeedbackById(feedbackId, request);
    }

    /**
     * update feedback by feedback ID
     */
    @PutMapping("/update/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackModel>> updateFeedback(@PathVariable String feedbackId, @RequestBody FeedbackModel feedbackModel) {
        log.info("Update feedback endpoint hit with ID: {}", feedbackId);
        return feedbackService.updateFeedbackById(feedbackId, feedbackModel);
    }

}
