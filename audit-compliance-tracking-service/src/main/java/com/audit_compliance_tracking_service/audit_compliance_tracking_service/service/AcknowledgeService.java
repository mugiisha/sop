package com.audit_compliance_tracking_service.audit_compliance_tracking_service.service;

import com.audit_compliance_tracking_service.audit_compliance_tracking_service.dto.AcknowledgedDto;
import com.audit_compliance_tracking_service.audit_compliance_tracking_service.dto.ApiResponse;
import com.audit_compliance_tracking_service.audit_compliance_tracking_service.model.AcknowledgeModel;
import com.audit_compliance_tracking_service.audit_compliance_tracking_service.repository.AcknowledgeRepository;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AcknowledgeService {

    private static final Logger log = LoggerFactory.getLogger(AcknowledgeService.class);


    private AcknowledgeRepository acknowledgeRepository;
    private final UserClientService userClientService;

    @Autowired
    public AcknowledgeService(AcknowledgeRepository acknowledgeRepository , UserClientService userClientService) {
        this.acknowledgeRepository = acknowledgeRepository;
        this.userClientService = userClientService;

    }

    // Method to fetch user information by user ID
    private getUserInfoResponse fetchUserInfo(String userId) {
        return userClientService.getUserInfo(userId);
    }


    @KafkaListener(
            topics = "sop-created",
            groupId = "audit-compliance-tracking-service"
    )

    /**
     * Consumer method for the Kafka topic sop-created
     */
    public void AcknowledgedCreatedListener(String data) throws JsonProcessingException {
        log.info("Received SOP created event: {}", data);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // Convert the incoming data to a FeedbackDto using the ObjectMapper
            AcknowledgedDto acknowledgedDto = objectMapper.readValue(data, AcknowledgedDto.class);

            // Create a new AcknowledgedDto and set its fields from the DTO
            AcknowledgeModel acknowledgeModel = new AcknowledgeModel();
            acknowledgeModel.setId(new ObjectId().toString());
            acknowledgeModel.setSopId(acknowledgedDto.getId());
            acknowledgeModel.setTitle(acknowledgedDto.getTitle());
            acknowledgeModel.setInitiatedBy(acknowledgedDto.getInitiatedBy());
            acknowledgeModel.setDepartmentId(acknowledgedDto.getDepartmentId());
            acknowledgeModel.setStatus(acknowledgedDto.getStatus());
            acknowledgeModel.setTimestamp(new Date());
            acknowledgeModel.setAcknowledgedBy(new ArrayList<>());

            // Save the AcknowledgeModel to the database
            acknowledgeRepository.save(acknowledgeModel);

            log.info("Saved AcknowledgeModel: {}", acknowledgeModel);
        } catch (Exception e) {
            log.error("Error processing SOP created event: {}", e.getMessage(), e);
        }


    }


    /**
     * Acknowledge an SOP.
     */
    public ResponseEntity<ApiResponse<AcknowledgeModel>> acknowledgeSOP(String sopId, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        log.info("userId: {}", userId);

        if (userId == null || userId.isEmpty()) {
            ApiResponse<AcknowledgeModel> response = new ApiResponse<>("User ID header is missing", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        getUserInfoResponse userInfoResponse = fetchUserInfo(userId);
        log.info("User Info Response: {}", userInfoResponse);
        String userName = userInfoResponse.getName();

        Optional<AcknowledgeModel> acknowledgeModelOptional = acknowledgeRepository.findBySopId(sopId);

        if (acknowledgeModelOptional.isPresent()) {
            AcknowledgeModel acknowledgeModel = acknowledgeModelOptional.get();
            // Fetch the actual name for initiatedBy
            String initiatedBy = acknowledgeModel.getInitiatedBy();
            getUserInfoResponse initiatedByUserInfoResponse = fetchUserInfo(initiatedBy);
            String initiatedByName = initiatedByUserInfoResponse.getName();
            acknowledgeModel.setInitiatedBy(initiatedByName);

            List<String> acknowledgedBy = acknowledgeModel.getAcknowledgedBy();
            if (!acknowledgedBy.contains(userName)) {
                acknowledgedBy.add(userName);
                acknowledgeModel.setAcknowledgedBy(acknowledgedBy);
                acknowledgeRepository.save(acknowledgeModel);
            }
            ApiResponse<AcknowledgeModel> response = new ApiResponse<>("SOP acknowledged by " + userName, acknowledgeModel);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ApiResponse<AcknowledgeModel> response = new ApiResponse<>("SOP not found", null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }}


    /**
     * Get all acknowledgements.
     */
    public ResponseEntity<ApiResponse<List<AcknowledgeModel>>> getAllAcknowledged() {
        try {
            List<AcknowledgeModel> acknowledgements = acknowledgeRepository.findAll();
            ApiResponse<List<AcknowledgeModel>> response = new ApiResponse<>("All acknowledgements retrieved successfully", acknowledgements);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving all acknowledgements: {}", e.getMessage(), e);
            ApiResponse<List<AcknowledgeModel>> response = new ApiResponse<>("Failed to retrieve all acknowledgements: " + e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }










}
