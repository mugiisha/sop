package com.sop_content_service.sop_content_service.repository;
import com.sop_content_service.sop_content_service.model.FeedbackModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedbackRepository extends MongoRepository<FeedbackModel, String> {
    List<FeedbackModel> findBySopId(String sopId); // Find all feedbacks by SOP ID

    List<FeedbackModel> findByUserId(ObjectId userId); // Find all feedbacks by User ID
}
