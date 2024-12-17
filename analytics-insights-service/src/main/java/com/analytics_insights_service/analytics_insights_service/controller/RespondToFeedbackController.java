package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.dto.FeedbackResponseDTO;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feedback/respond")
public class RespondToFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PutMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackModel>> addResponse(
            @PathVariable String feedbackId,
            @RequestBody FeedbackResponseDTO feedbackResponseDTO) {

        // Extract the actual response string
        String response = feedbackResponseDTO.getResponse();

        // Call the service method with the clean response
        return feedbackService.addResponse(feedbackId, response);
    }


}