package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.dto.FeedbackDto;
import com.analytics_insights_service.analytics_insights_service.exception.SopNotFoundException;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import userService.getUserInfoResponse;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);


    private final FeedbackRepository feedbackRepository;
    private final UserClientService userClientService;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, UserClientService userClientService) {
        this.feedbackRepository = feedbackRepository;
        this.userClientService = userClientService;
    }

    // Method to fetch user information by user ID
    private getUserInfoResponse fetchUserInfo(String userId) {
        return userClientService.getUserInfo(userId);
    }

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
     * Create feedback based on sopId
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> createFeedback(String sopId, FeedbackModel feedbackModel, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        String departmentId = request.getHeader("X-Department-Id");
        log.info("departmentId: {}", departmentId);

        if (userId == null || userId.isEmpty()) {
            ApiResponse<FeedbackModel> response = new ApiResponse<>("User ID header is missing", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Check if the user role is HOD and return a response if it is
        if ("HOD".equalsIgnoreCase(userRole)) {
            ApiResponse<FeedbackModel> response = new ApiResponse<>("HoD cannot add feedback", null);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        try {
            boolean sopExists = feedbackRepository.existsBySopId(sopId);
            if (sopExists) {
                getUserInfoResponse userInfoResponse = fetchUserInfo(userId);
                log.info("User Info Response: {}", userInfoResponse);
                String userName = userInfoResponse.getName();
                String profilePic = userInfoResponse.getProfilePictureUrl();
                String departmentName = userInfoResponse.getDepartmentName();

                List<FeedbackModel> existingFeedbacks = feedbackRepository.findBySopId(sopId);

                boolean userFeedbackExists = existingFeedbacks.stream()
                        .anyMatch(feedback -> userName != null && userName.equals(feedback.getUserName()));

                if (userFeedbackExists) {
                    Optional<FeedbackModel> existingFeedbackOptional = existingFeedbacks.stream()
                            .filter(feedback -> userName != null && userName.equals(feedback.getUserName()))
                            .findFirst();

                    if (existingFeedbackOptional.isPresent()) {
                        // User has already added feedback for this SOP
                        ApiResponse<FeedbackModel> response = new ApiResponse<>("You cannot add feedback twice on this SOP. Please update your existing feedback.", null);
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    FeedbackModel newFeedback = new FeedbackModel();
                    newFeedback.setSopId(sopId);
                    newFeedback.setTitle(
                            !existingFeedbacks.isEmpty() ? existingFeedbacks.get(0).getTitle() : feedbackModel.getTitle()
                    );
                    newFeedback.setUserName(userName);
                    newFeedback.setRole(userRole);
                    newFeedback.setProfilePic(profilePic);
                    newFeedback.setDepartmentName(departmentName);
                    newFeedback.setContent(feedbackModel.getContent());
                    newFeedback.setTimestamp(new Date());

                    FeedbackModel savedFeedback = feedbackRepository.save(newFeedback);
                    deleteNullFeedbacks(sopId);
                    ApiResponse<FeedbackModel> response = new ApiResponse<>("New feedback created successfully", savedFeedback);
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                }
            } else {
                ApiResponse<FeedbackModel> response = new ApiResponse<>("SOP ID does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error creating feedback: ", e);
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Failed to create feedback: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Add a default return statement as a fallback
        ApiResponse<FeedbackModel> response = new ApiResponse<>("Unexpected error occurred", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Delete feedbacks with null content
     */
    private void deleteNullFeedbacks(String sopId) {
        List<FeedbackModel> feedbacks = feedbackRepository.findBySopId(sopId);
        feedbacks.stream()
                .filter(feedback -> feedback.getContent() == null || feedback.getContent().isEmpty())
                .forEach(feedbackRepository::delete);
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
     * Add response to feedback
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> addResponse(String feedbackId, String response, HttpServletRequest request) {
        try {
            String userRole = request.getHeader("X-User-Role");

            // Check if the user role is not HOD
            if (!"HOD".equalsIgnoreCase(userRole)) {
                ApiResponse<FeedbackModel> apiResponse = new ApiResponse<>("Only HOD can add a response to this feedback", null);
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }

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
    public ResponseEntity<ApiResponse<Void>> deleteFeedbackById(String feedbackId, HttpServletRequest request) {
        try {
            String userRole = request.getHeader("X-User-Role");
            log.info("User role: {}", userRole);

            // Check if the user role is not HOD
            if (!"HOD".equalsIgnoreCase(userRole)) {
                ApiResponse<Void> apiResponse = new ApiResponse<>("Only HOD can delete this feedback", null);
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }

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

    /**
     * Update feedback by ID
     */
    public ResponseEntity<ApiResponse<FeedbackModel>> updateFeedbackById(String feedbackId, FeedbackModel feedbackModel) {
        try {
            log.info("Updating feedback with ID: {}", feedbackId);
            log.info("Incoming feedback content: {}", feedbackModel.getContent());
            // Check if feedback exists
            FeedbackModel feedback = feedbackRepository.findById(feedbackId)
                    .orElse(null);

            if (feedback == null) {
                ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback with ID " + feedbackId + " does not exist", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Update feedback
            feedback.setContent(feedbackModel.getContent());
            feedback.setTimestamp(new Date());

            FeedbackModel updatedFeedback = feedbackRepository.save(feedback);

            ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback updated successfully", updatedFeedback);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<FeedbackModel> response = new ApiResponse<>("Failed to update feedback: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }




    }
}
