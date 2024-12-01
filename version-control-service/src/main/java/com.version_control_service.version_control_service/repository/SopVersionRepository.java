package com.version_control_service.version_control_service.repository;

import com.version_control_service.version_control_service.model.SopVersionModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SopVersionRepository extends MongoRepository<SopVersionModel, String> {
    // Find all versions by SOP ID
    List<SopVersionModel> findBySopId(ObjectId sopId);
//    List<SopVersionModel> findBySopIds(String sopId); // Reference to SOP ID

    // Get the latest version for a SOP
    SopVersionModel findTopBySopIdOrderByCreatedAtDesc(ObjectId sopId);

    // Find specific version by SOP ID and version number
    SopVersionModel findBySopIdAndVersionNumber(ObjectId sopId, String versionNumber);
}
