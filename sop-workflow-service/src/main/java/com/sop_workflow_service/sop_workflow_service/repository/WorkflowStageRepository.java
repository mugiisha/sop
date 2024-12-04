package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowStageRepository extends MongoRepository<WorkflowStage, String> {
    List<WorkflowStage> findBySopId(String sopId);
}
