package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.dto.FeedbackDto;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import userService.getUserInfoResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private UserClientService userClientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackModel mockFeedback;
    private String testSopId;
    private String testUserId;
    private String testUserName;

    @BeforeEach
    void setUp() {
        testSopId = "test-sop-id";
        testUserId = "test-user-id";
        testUserName = "Test User";

        mockFeedback = new FeedbackModel();
        mockFeedback.setId("test-feedback-id");
        mockFeedback.setSopId(testSopId);
        mockFeedback.setTitle("Test SOP");
        mockFeedback.setContent("Test feedback content");
        mockFeedback.setUserName(testUserName);
        mockFeedback.setTimestamp(new Date());
    }

    @Test
    void createFeedback_Success() {
        // Arrange
        when(request.getHeader("X-User-Id")).thenReturn(testUserId);
        when(request.getHeader("X-User-Role")).thenReturn("USER");
        when(request.getHeader("X-Department-Id")).thenReturn("test-dept-id");

        when(feedbackRepository.existsBySopId(testSopId)).thenReturn(true);
        when(feedbackRepository.findBySopId(testSopId)).thenReturn(new ArrayList<>());

        getUserInfoResponse userInfo = getUserInfoResponse.newBuilder()
                .setName(testUserName)
                .setProfilePictureUrl("test-url")
                .setDepartmentName("Test Department")
                .build();
        when(userClientService.getUserInfo(testUserId)).thenReturn(userInfo);

        when(feedbackRepository.save(any(FeedbackModel.class))).thenReturn(mockFeedback);

        // Act
        ResponseEntity<ApiResponse<FeedbackModel>> response =
                feedbackService.createFeedback(testSopId, mockFeedback, request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New feedback created successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void createFeedback_HOD_Forbidden() {
        // Arrange
        when(request.getHeader("X-User-Id")).thenReturn(testUserId);
        when(request.getHeader("X-User-Role")).thenReturn("HOD");

        // Act
        ResponseEntity<ApiResponse<FeedbackModel>> response =
                feedbackService.createFeedback(testSopId, mockFeedback, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("HoD cannot add feedback", response.getBody().getMessage());
    }

    @Test
    void getFeedbacksBySopId_Success() {
        // Arrange
        List<FeedbackModel> feedbackList = Collections.singletonList(mockFeedback);
        when(feedbackRepository.findBySopId(testSopId)).thenReturn(feedbackList);

        // Act
        ResponseEntity<ApiResponse<List<FeedbackModel>>> response =
                feedbackService.getFeedbacksBySopId(testSopId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Feedbacks retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getFeedbacksBySopId_NotFound() {
        // Arrange
        when(feedbackRepository.findBySopId(testSopId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<List<FeedbackModel>>> response =
                feedbackService.getFeedbacksBySopId(testSopId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No feedbacks found for the given SOP ID", response.getBody().getMessage());
    }

    @Test
    void getFeedbackById_Success() {
        // Arrange
        when(feedbackRepository.findById(mockFeedback.getId())).thenReturn(Optional.of(mockFeedback));

        // Act
        ResponseEntity<ApiResponse<FeedbackModel>> response =
                feedbackService.getFeedbackById(mockFeedback.getId());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Feedback retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void addResponse_Success() {
        // Arrange
        when(request.getHeader("X-User-Role")).thenReturn("HOD");
        when(feedbackRepository.findById(mockFeedback.getId())).thenReturn(Optional.of(mockFeedback));
        when(feedbackRepository.save(any(FeedbackModel.class))).thenReturn(mockFeedback);

        String response = "Test response";

        // Act
        ResponseEntity<ApiResponse<FeedbackModel>> result =
                feedbackService.addResponse(mockFeedback.getId(), response, request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Response added successfully", result.getBody().getMessage());
    }

    @Test
    void addResponse_NotHOD_Forbidden() {
        // Arrange
        when(request.getHeader("X-User-Role")).thenReturn("USER");

        // Act
        ResponseEntity<ApiResponse<FeedbackModel>> result =
                feedbackService.addResponse(mockFeedback.getId(), "Test response", request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertEquals("Only HOD can add a response to this feedback", result.getBody().getMessage());
    }

    @Test
    void updateFeedbackById_Success() {
        // Arrange
        FeedbackModel updatedFeedback = new FeedbackModel();
        updatedFeedback.setContent("Updated content");

        when(feedbackRepository.findById(mockFeedback.getId())).thenReturn(Optional.of(mockFeedback));
        when(feedbackRepository.save(any(FeedbackModel.class))).thenReturn(mockFeedback);

        // Act
        ResponseEntity<ApiResponse<FeedbackModel>> response =
                feedbackService.updateFeedbackById(mockFeedback.getId(), updatedFeedback);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Feedback updated successfully", response.getBody().getMessage());
    }

    @Test
    void deleteFeedbackById_Success() {
        // Arrange
        when(request.getHeader("X-User-Role")).thenReturn("HOD");
        when(feedbackRepository.findById(mockFeedback.getId())).thenReturn(Optional.of(mockFeedback));
        doNothing().when(feedbackRepository).delete(mockFeedback);

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                feedbackService.deleteFeedbackById(mockFeedback.getId(), request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Feedback deleted successfully", response.getBody().getMessage());
        verify(feedbackRepository).delete(mockFeedback);
    }

    @Test
    void deleteFeedbackById_NotHOD_Forbidden() {
        // Arrange
        when(request.getHeader("X-User-Role")).thenReturn("USER");

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                feedbackService.deleteFeedbackById(mockFeedback.getId(), request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only HOD can delete this feedback", response.getBody().getMessage());
    }

    @Test
    void FeedbackCreatedListener_Success() throws Exception {
        // Arrange
        String jsonData = "{\"id\":\"test-id\",\"title\":\"Test Title\"}";
        FeedbackDto dto = new FeedbackDto();
        dto.setId("test-id");
        dto.setTitle("Test Title");

        ObjectMapper realMapper = new ObjectMapper();
        when(objectMapper.readValue(jsonData, FeedbackDto.class)).thenReturn(dto);
        when(feedbackRepository.save(any(FeedbackModel.class))).thenReturn(mockFeedback);

        // Act
        feedbackService.FeedbackCreatedListener(jsonData);

        // Assert
        verify(feedbackRepository).save(any(FeedbackModel.class));
    }
}