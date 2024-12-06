package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.exception.SopException;
import com.sop_content_service.sop_content_service.model.FeedbackModel;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.FeedbackRepository;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private SopRepository sopRepository;

    /**
     * Create a new feedback
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> createFeedback(String sopId, String userId, FeedbackModel feedbackModel) {
        try {
            // Check if the sopId exists in the SopRepository
            Optional<SopModel> existingSop = sopRepository.findById(sopId);

            if (!existingSop.isPresent()) {
                // If SOP does not exist, return a NotFound response
                ApiResponse<FeedbackModel> response = new ApiResponse<>("SOP with the given ID does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Convert userId to ObjectId (assuming you're using ObjectId for userId)
            feedbackModel.setSopId(sopId); // Set sopId to the feedback
            feedbackModel.setUserId(userId); // Set userId to the feedback

            // Save feedback to the database
            FeedbackModel createdFeedback = feedbackRepository.save(feedbackModel);

            // Return success response
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback created successfully", createdFeedback);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            // Handle any unexpected errors
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Failed to create feedback: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get all feedbacks for a specific SOP
     */
    public ResponseEntity<ApiResponse<List<FeedbackModel>>> getFeedbacksBySopId(String sopId) {
        try {
            List<FeedbackModel> feedbacks = feedbackRepository.findBySopId(sopId);
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Feedbacks retrieved successfully", feedbacks);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve feedbacks for SOP ID: " + sopId);
        }
    }

    /**
     * Get feedback by feedback ID
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> getFeedbackById(String feedbackId) {
        try {
            FeedbackModel feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new SopException.SopNotFoundException("Feedback not found with ID: " +feedbackId));
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback retrieved successfully", feedback);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve feedback: " + feedbackId);
        }
    }

    /**
     * Get all feedbacks by user ID
     */
    public ResponseEntity<ApiResponse<List<FeedbackModel>>> getFeedbacksByUserId(String userId) {
        try {
            List<FeedbackModel> feedbacks = feedbackRepository.findByUserId(new ObjectId(userId));
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Feedbacks retrieved successfully", feedbacks);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve feedbacks for user ID: " + userId);
        }
    }
}
