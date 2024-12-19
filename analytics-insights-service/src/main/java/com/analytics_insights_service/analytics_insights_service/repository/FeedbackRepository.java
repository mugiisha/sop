package com.analytics_insights_service.analytics_insights_service.repository;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface FeedbackRepository extends MongoRepository<FeedbackModel, String> {
    List<FeedbackModel> findBySopId(String sopId); // Find all feedbacks by SOP ID

    List<FeedbackModel> findByUserId(String userId);

    boolean existsBySopId(String sopId);

    // Query to filter feedbacks by timestamp range
    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<FeedbackModel> findByTimestampBetween(Date startDate, Date endDate);
}
