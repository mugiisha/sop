package com.audit_compliance_tracking_service.audit_compliance_tracking_service.repository;

import com.audit_compliance_tracking_service.audit_compliance_tracking_service.model.AcknowledgeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface  AcknowledgeRepository extends MongoRepository<AcknowledgeModel,String> {
    Optional<AcknowledgeModel> findBySopId(String sopId);
}
