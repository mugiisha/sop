package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.model.Approver;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SOPRepository extends MongoRepository<SOP, String> {

    List<Approver> findAllById(List<String> ids);
}
