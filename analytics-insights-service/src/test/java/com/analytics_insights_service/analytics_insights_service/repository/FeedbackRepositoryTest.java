package com.analytics_insights_service.analytics_insights_service.repository;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class FeedbackRepositoryTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @BeforeEach
    public void setUp() {
        feedbackRepository.deleteAll();
    }

    @Test
    public void testFindBySopId() {
        FeedbackModel feedback1 = new FeedbackModel("sop123", "John Doe", "profilePicUrl", "Developer", "Engineering", "This is feedback content 1", "This is response 1", "Feedback Title 1");
        FeedbackModel feedback2 = new FeedbackModel("sop123", "Jane Doe", "profilePicUrl2", "Tester", "QA", "This is feedback content 2", "This is response 2", "Feedback Title 2");
        feedbackRepository.save(feedback1);
        feedbackRepository.save(feedback2);

        List<FeedbackModel> feedbacks = feedbackRepository.findBySopId("sop123");

        assertEquals(2, feedbacks.size());
    }

    @Test
    public void testExistsBySopId() {
        FeedbackModel feedback = new FeedbackModel("sop123", "John Doe", "profilePicUrl", "Developer", "Engineering", "This is feedback content", "This is response", "Feedback Title");
        feedbackRepository.save(feedback);

        boolean exists = feedbackRepository.existsBySopId("sop123");

        assertTrue(exists);
    }

    @Test
    public void testFindByTimestampBetween() {
        Date now = new Date();
        Date oneHourAgo = new Date(now.getTime() - 3600 * 1000);
        Date twoHoursAgo = new Date(now.getTime() - 2 * 3600 * 1000);

        FeedbackModel feedback1 = new FeedbackModel("sop123", "John Doe", "profilePicUrl", "Developer", "Engineering", "This is feedback content 1", "This is response 1", "Feedback Title 1");
        feedback1.setTimestamp(oneHourAgo);
        FeedbackModel feedback2 = new FeedbackModel("sop123", "Jane Doe", "profilePicUrl2", "Tester", "QA", "This is feedback content 2", "This is response 2", "Feedback Title 2");
        feedback2.setTimestamp(twoHoursAgo);
        feedbackRepository.save(feedback1);
        feedbackRepository.save(feedback2);

        List<FeedbackModel> feedbacks = feedbackRepository.findByTimestampBetween(twoHoursAgo, now);

        assertEquals(2, feedbacks.size());
    }

    @Test
    public void testSaveAndFindById() {
        FeedbackModel feedback = new FeedbackModel("sop123", "John Doe", "profilePicUrl", "Developer", "Engineering", "This is feedback content", "This is response", "Feedback Title");
        feedbackRepository.save(feedback);

        Optional<FeedbackModel> retrievedFeedback = feedbackRepository.findById(feedback.getId());

        assertTrue(retrievedFeedback.isPresent());
        assertEquals(feedback.getId(), retrievedFeedback.get().getId());
    }
}