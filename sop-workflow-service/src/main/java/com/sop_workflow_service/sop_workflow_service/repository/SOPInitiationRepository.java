package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.SOPInitiation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SOPInitiationRepository extends MongoRepository<SOPInitiation, String> {
    List<SOPInitiation> findByStatus(String status);
}
