package com.version_control_service.version_control_service.repository;

import com.version_control_service.version_control_service.model.Version;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends MongoRepository<Version, String> {
    Version findFirstBySopIdAndCurrentVersion(String sopId, boolean isCurrentVersion);
    Version findFirstBySopIdAndVersionNumber(String sopId,Float versionNumber);
    Version findTopBySopIdOrderByVersionNumberDesc(String sopId);
    List<Version> findAllBySopIdAndVersionNumberIn(String sopId,List<Float> versionNumbers);
    List<Version> findAllBySopId(String sopId);
}
