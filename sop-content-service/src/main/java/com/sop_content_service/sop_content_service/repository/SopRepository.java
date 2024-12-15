package com.sop_content_service.sop_content_service.repository;

import com.sop_content_service.sop_content_service.enums.Visibility;
import com.sop_content_service.sop_content_service.model.Sop;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SopRepository extends MongoRepository<Sop, String> {
    List<Sop> findAllByOrderByCreatedAtDesc();
    boolean existsById(String id);
    List<Sop> findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(UUID departmentId, Visibility visibility);


}
