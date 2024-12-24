package com.version_control_service.version_control_service.repository;

import com.version_control_service.version_control_service.model.SOP;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SopRepository extends MongoRepository<SOP, String> {
}
