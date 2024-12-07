package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowStageRepository extends MongoRepository<WorkflowStage, String> {
    List<WorkflowStage> findBySopId(String sopId);
    Optional<WorkflowStage> findFirstBySopIdAndUserId(String sopId, UUID userId);
}
