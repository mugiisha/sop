package com.version_control_service.version_control_service.repository;

import com.version_control_service.version_control_service.model.SopVersionModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SopVersionRepository extends MongoRepository<SopVersionModel, String> {
    List<SopVersionModel> findBySopId(String sopId);
    SopVersionModel findTopBySopIdOrderByCreatedAtDesc(String sopId); // Get the latest version
    SopVersionModel findBySopIdAndVersionNumber(String sopId, String versionNumber);
}
