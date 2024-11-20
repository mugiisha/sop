package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.Reviewer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewerRepository extends MongoRepository<Reviewer, String> {
}
