package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SOPRepository extends MongoRepository<SOP, String> {
}
