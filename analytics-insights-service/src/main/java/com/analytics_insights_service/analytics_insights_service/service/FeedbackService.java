package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.dto.FeedbackDto;
import com.analytics_insights_service.analytics_insights_service.exception.SopNotFoundException;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.analytics_insights_service.analytics_insights_service.util.DtoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    /**
     * Consumer method for the Kafka topic sop-created
     */
    public void FeedbackCreatedListener(String data) throws JsonProcessingException {
        log.info("Received SOP created event: {}", data);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Convert the incoming data to a FeedbackDto using the ObjectMapper
            FeedbackDto feedbackDto = objectMapper.readValue(data, FeedbackDto.class);

            // Create a new FeedbackModel and set its fields from the DTO
            FeedbackModel feedbackModel = new FeedbackModel();
            feedbackModel.setId(new ObjectId().toString());
            feedbackModel.setSopId(feedbackDto.getId());
            feedbackModel.setTitle(feedbackDto.getTitle());

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
            // Check if the sopId exists in any feedback
            boolean sopExists = feedbackRepository.existsBySopId(sopId);
            if (!sopExists) {
                ApiResponse<FeedbackModel> response = new ApiResponse<>("SOP ID does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            // Check if feedback with the same sopId already exists
            List<FeedbackModel> existingFeedbacks = feedbackRepository.findBySopId(sopId);
            if (!existingFeedbacks.isEmpty()) {
                // Update the existing feedback
                FeedbackModel existingFeedback = existingFeedbacks.get(0); // Assuming only one feedback per sopId
                existingFeedback.setContent(feedbackModel.getContent());
                existingFeedback.setUserId(userId);
                existingFeedback.setTimestamp(new Date()); // Update the timestamp to the current time
                existingFeedback.setResponse(null); // Set response to null
                FeedbackModel updatedFeedback = feedbackRepository.save(existingFeedback);

                ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback Created Successfully", updatedFeedback);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                // Feedback with the given sopId does not exist
                ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback with the given SOP ID does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Failed to update feedback: " + e.getMessage(), null);
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
     * Get specific feedback By feedbackId
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> getFeedbackById(String feedbackId) {
        try {
            // Fetch feedback by ID
            FeedbackModel feedback = feedbackRepository.findById(feedbackId)
                    .orElse(null);

            // If no feedback is found, return a 404 response
            if (feedback == null) {
                ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback not found with ID: " + feedbackId, null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Return a success response if feedback is found
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback retrieved successfully", feedback);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Return a 500 response for other errors
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Failed to retrieve feedback: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get feedback By userId
     */
    public ResponseEntity<ApiResponse<List<FeedbackModel>>> getFeedbacksByUserId(String userId) {
        try {
            // Fetch feedbacks by user ID
            List<FeedbackModel> feedbacks = feedbackRepository.findByUserId(userId);

            // If the list is empty, return a 404 response
            if (feedbacks.isEmpty()) {
                ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("No feedbacks found for the given user ID:" +userId,null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Return a success response if feedbacks are found
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Feedbacks retrieved successfully", feedbacks);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Return a 500 response for other errors
            ApiResponse<List<FeedbackModel>> response = new ApiResponse<>("Failed to retrieve feedbacks for user ID: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Add response to feedback
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> addResponse(String feedbackId, String response) {
        try {
            // Fetch feedback by ID
            FeedbackModel feedback = feedbackRepository.findById(feedbackId)
                    .orElse(null);

            // If no feedback is found, return a 404 response
            if (feedback == null) {
                ApiResponse<FeedbackModel> apiResponse = new ApiResponse<>("Feedback not found with ID: " + feedbackId, null);
                return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
            }

            // Set the new response field
            feedback.setResponse(response);

            // Save the updated feedback back to the database
            FeedbackModel updatedFeedback = feedbackRepository.save(feedback);

            // Create a success response
            ApiResponse<FeedbackModel> apiResponse = new ApiResponse<>("Response added successfully", updatedFeedback);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            // Return a 500 response for other errors
            ApiResponse<FeedbackModel> apiResponse = new ApiResponse<>("Failed to add response: " + e.getMessage(), null);
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete feedback by ID
     */
    public ResponseEntity<ApiResponse<Void>> deleteFeedbackById(String feedbackId) {
        try {
            // Check if feedback exists
            FeedbackModel feedback = feedbackRepository.findById(feedbackId)
                    .orElse(null);

            if (feedback == null) {
                ApiResponse<Void> response = new ApiResponse<>("Feedback with ID " + feedbackId + " does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Delete feedback
            feedbackRepository.delete(feedback);

            ApiResponse<Void> response = new ApiResponse<>("Feedback deleted successfully", null);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<Void> response = new ApiResponse<>("Failed to delete feedback: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
