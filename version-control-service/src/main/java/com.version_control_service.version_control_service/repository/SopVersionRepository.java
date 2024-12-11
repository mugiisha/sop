//package com.version_control_service.version_control_service.repository;
//
//import com.version_control_service.version_control_service.model.SopVersionModel;
//import org.bson.types.ObjectId;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import java.util.List;
//import java.util.Optional;
//
//public interface SopVersionRepository extends MongoRepository<SopVersionModel, String> {
//    // Find all versions by SOP ID with sorting
//    List<SopVersionModel> findBySopId(ObjectId sopId, Sort sort);
//
//    // Find the latest version by creation date
//    Optional<SopVersionModel> findTopBySopIdOrderByCreatedAtDesc(ObjectId sopId);
//
//    // Find specific version by SOP ID and version number
//    Optional<SopVersionModel> findBySopIdAndVersionNumber(ObjectId sopId, String versionNumber);
//
//    // Check if version number already exists for a SOP
//    boolean existsBySopIdAndVersionNumber(ObjectId sopId, String versionNumber);
//
//    // Find all versions by SOP ID and visibility
//    List<SopVersionModel> findBySopIdAndVisibility(ObjectId sopId, String visibility, Sort sort);
//
//    // Find by code (useful for unique code validation)
//    Optional<SopVersionModel> findByCode(String code);
//
//    // Custom query to find versions by category
//    @Query("{'sopId': ?0, 'category': ?1}")
//    List<SopVersionModel> findBySopIdAndCategory(ObjectId sopId, String category, Sort sort);
//
//    // Count versions for a specific SOP
//    long countBySopId(ObjectId sopId);
//}