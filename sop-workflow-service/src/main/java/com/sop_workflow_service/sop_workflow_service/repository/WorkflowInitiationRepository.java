package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowInitiation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowInitiationRepository extends MongoRepository<WorkflowInitiation, String> {
    List<WorkflowInitiation> findBySopId(String sopId);
}
