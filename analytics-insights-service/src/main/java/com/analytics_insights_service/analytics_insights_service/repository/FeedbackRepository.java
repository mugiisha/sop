package com.analytics_insights_service.analytics_insights_service.repository;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends MongoRepository<FeedbackModel, String> {
    List<FeedbackModel> findBySopId(String sopId); // Find all feedbacks by SOP ID

    boolean existsBySopId(String sopId); // Check if feedback exists by SOP ID

    // Query to filter feedbacks by timestamp range
    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<FeedbackModel> findByTimestampBetween(Date startDate, Date endDate);
}
