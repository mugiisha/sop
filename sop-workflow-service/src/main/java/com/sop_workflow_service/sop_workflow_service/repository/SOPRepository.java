package com.sop_workflow_service.sop_workflow_service.repository;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Visibility;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SOPRepository extends MongoRepository<SOP, String> {
    List<SOP> findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(UUID departmentId, Visibility visibility);
    List<SOP> findAllByOrderByCreatedAtDesc();
    List<SOP> findAllByDepartmentId(UUID departmentId);
}
