package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.dto.ApiResponse;
import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.analytics_insights_service.analytics_insights_service.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
public class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedbackService feedbackService;

    private FeedbackModel feedbackModel;

    @MockBean
    private FeedbackRepository feedbackRepository;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        feedbackModel = new FeedbackModel(
                "sop123", "John Doe", "profilePicUrl", "Developer", "Engineering",
                "Feedback content", "Response content", "Feedback Title"
        );
    }

    @Test
    public void testCreateFeedback() throws Exception {
        ApiResponse<FeedbackModel> response = new ApiResponse<>("Feedback created successfully", feedbackModel);
        when(feedbackService.createFeedback(anyString(), any(FeedbackModel.class), any()))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/feedback/sop123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sopId": "sop123",
                                  "title": "Feedback Title",
                                  "userName": "John Doe",
                                  "role": "Developer",
                                  "departmentName": "Engineering",
                                  "profilePic": "profilePicUrl",
                                  "content": "Feedback content",
                                  "response": "Response content"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Feedback created successfully"))
                .andExpect(jsonPath("$.data.sopId").value("sop123"));
    }

    @Test
    public void testGetFeedbacksBySopId() throws Exception {
        ApiResponse<List<FeedbackModel>> response = new ApiResponse<>( "Feedbacks retrieved successfully", Collections.singletonList(feedbackModel));
        when(feedbackService.getFeedbacksBySopId(anyString())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/feedback/sop/sop123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sopId").value("sop123"));
    }

    @Test
    public void testGetFeedbackById() throws Exception {
        ApiResponse<FeedbackModel> response = new ApiResponse<>( "Feedback retrieved successfully", feedbackModel);
        when(feedbackService.getFeedbackById(anyString())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/feedback/feedbackId123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sopId").value("sop123"));
    }

    @Test
    public void testDeleteFeedback() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>("Feedback deleted successfully", null);
        when(request.getHeader("X-User-Role")).thenReturn("HOD");
        when(feedbackService.deleteFeedbackById(anyString(), eq(request))).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/feedback/delete/feedbackId123")
                        .header("X-User-Role", "HOD"))
                .andExpect(status().isOk());
    }
}
