package com.version_control_service.version_control_service.repository;

import com.version_control_service.version_control_service.model.VersionContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionContentRepository extends MongoRepository<VersionContent, String> {
}
