package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.dto.FeedbackDto;
import com.analytics_insights_service.analytics_insights_service.exception.SopNotFoundException;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.analytics_insights_service.analytics_insights_service.util.DtoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @KafkaListener(
            topics = "sop-created",
            groupId = "analytics-insights-service"
    )
    public void FeedbackCreatedListener(String data) throws JsonProcessingException {
        log.info("Received SOP created event: {}", data);
        try {
            // Convert the incoming data to a DTO or directly to a FeedbackModel

            // Use a Map to parse the JSON and extract only required fields
            Map<String, Object> jsonData = new ObjectMapper().readValue(data, Map.class);
            String id = (String) jsonData.get("id");

            FeedbackDto feedbackDto = new FeedbackDto(id);

            FeedbackModel feedbackModel = new FeedbackModel();
            feedbackModel.setId(feedbackDto.getId()); // Set the ID from the DTO


            // Save the model to the database
            feedbackRepository.save(feedbackModel);
            log.info("Saved Feedback model: {}", feedbackModel);
        } catch (Exception e) {
            log.error("Error processing SOP created event: {}", e.getMessage(), e);
        }
    }


    /**
     * Create a new feedback
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> createFeedback(String sopId, String userId, FeedbackModel feedbackModel) {
        try {
            // Check if the sopId exists
            boolean sopExists = feedbackRepository.existsById(sopId); // Assuming sopRepository is available and properly set up
            if (!sopExists) {
                // Throw an error if sopId does not exist
                ApiResponse<FeedbackModel> response = new ApiResponse<>("SOP ID does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Set sopId and userId to the feedbackModel
            feedbackModel.setSopId(sopId);
            feedbackModel.setUserId(userId);

            // Save the new feedback to the database
            FeedbackModel createdFeedback = feedbackRepository.save(feedbackModel);

            // Return success response with the created feedback
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback created successfully", createdFeedback);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            // Handle unexpected errors
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Failed to create feedback: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get all feedbacks for a specific SOP
     */
    public ResponseEntity<ApiResponse<List<FeedbackModel>>> getFeedbacksBySopId(String sopId) {
        try {
            // Retrieve the list of feedbacks for the given sopId
            List<FeedbackModel> feedbacks = feedbackRepository.findBySopId(sopId);

            if (feedbacks.isEmpty()) {
                // If no feedbacks are found, return a Not Found response
                ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("No feedbacks found for the given SOP ID", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Return success response with the list of feedbacks
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Feedbacks retrieved successfully", feedbacks);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Handle any unexpected errors and return Internal Server Error response
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Failed to retrieve feedbacks for SOP ID: " + sopId, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get feedback by feedback ID
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> getFeedbackById(String feedbackId) {
        try {
            FeedbackModel feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new SopNotFoundException("Feedback not found with ID: " +feedbackId));
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
            List<FeedbackModel> feedbacks = feedbackRepository.findByUserId(userId);
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Feedbacks retrieved successfully", feedbacks);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve feedbacks for user ID: " + userId);
        }
    }
}
