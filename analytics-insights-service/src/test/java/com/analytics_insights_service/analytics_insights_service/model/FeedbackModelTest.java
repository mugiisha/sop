package com.analytics_insights_service.analytics_insights_service.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FeedbackModelTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testModelGettersAndSetters() {
        FeedbackModel feedbackModel = new FeedbackModel();
        feedbackModel.setSopId("sop123");
        feedbackModel.setTitle("Feedback Title");
        feedbackModel.setUserName("John Doe");
        feedbackModel.setRole("Developer");
        feedbackModel.setDepartmentName("Engineering");
        feedbackModel.setProfilePic("profilePicUrl");
        feedbackModel.setContent("This is a feedback content.");
        feedbackModel.setResponse("This is a response.");

        assertEquals("sop123", feedbackModel.getSopId());
        assertEquals("Feedback Title", feedbackModel.getTitle());
        assertEquals("John Doe", feedbackModel.getUserName());
        assertEquals("Developer", feedbackModel.getRole());
        assertEquals("Engineering", feedbackModel.getDepartmentName());
        assertEquals("profilePicUrl", feedbackModel.getProfilePic());
        assertEquals("This is a feedback content.", feedbackModel.getContent());
        assertEquals("This is a response.", feedbackModel.getResponse());
    }

    @Test
    public void testModelConstructor() {
        FeedbackModel feedbackModel = new FeedbackModel(
                "sop123", "John Doe", "profilePicUrl", "Developer", "Engineering",
                "This is a feedback content.", "This is a response.", "Feedback Title"
        );

        assertEquals("sop123", feedbackModel.getSopId());
        assertEquals("Feedback Title", feedbackModel.getTitle());
        assertEquals("John Doe", feedbackModel.getUserName());
        assertEquals("Developer", feedbackModel.getRole());
        assertEquals("Engineering", feedbackModel.getDepartmentName());
        assertEquals("profilePicUrl", feedbackModel.getProfilePic());
        assertEquals("This is a feedback content.", feedbackModel.getContent());
        assertEquals("This is a response.", feedbackModel.getResponse());
    }

    @Test
    public void testModelValidation() {
        FeedbackModel feedbackModel = new FeedbackModel();
        feedbackModel.setSopId(""); // Invalid because it's blank
        feedbackModel.setTitle(""); // Invalid because it's blank
        feedbackModel.setUserName(null); // Invalid because it's null
        feedbackModel.setRole(null); // Invalid because it's null
        feedbackModel.setDepartmentName(null); // Invalid because it's null
        feedbackModel.setProfilePic(null); // Invalid because it's null
        feedbackModel.setContent(""); // Invalid because it's blank
        feedbackModel.setResponse("Valid Response");

        Set<jakarta.validation.ConstraintViolation<FeedbackModel>> violations = validator.validate(feedbackModel);

        // Print out the violations
        violations.forEach(violation -> System.out.println(violation.getPropertyPath() + " " + violation.getMessage()));

        // Update the expected count based on actual violations
        assertEquals(7, violations.size());
    }

    @Test
    public void testContentSizeValidation() {
        FeedbackModel feedbackModel = new FeedbackModel();
        feedbackModel.setContent("a".repeat(1001)); // Invalid because it exceeds 1000 characters

        Set<jakarta.validation.ConstraintViolation<FeedbackModel>> violations = validator.validate(feedbackModel);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Content must not exceed 1000 characters")));
    }

    @Test
    public void testResponseSizeValidation() {
        FeedbackModel feedbackModel = new FeedbackModel();
        feedbackModel.setResponse("a".repeat(1001)); // Invalid because it exceeds 1000 characters

        Set<jakarta.validation.ConstraintViolation<FeedbackModel>> violations = validator.validate(feedbackModel);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Response must not exceed 1000 characters")));
    }
}