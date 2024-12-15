//package com.version_control_service.version_control_service.service;
//
//import com.version_control_service.version_control_service.model.SopVersionModel;
//import com.version_control_service.version_control_service.repository.SopVersionRepository;
//import com.version_control_service.version_control_service.exception.SopNotFoundException;
//import com.version_control_service.version_control_service.dto.ApiResponse;
//import com.version_control_service.version_control_service.utils.SopVersionUtils;
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.data.domain.Sort;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class SopVersionService {
//
//    private final SopVersionRepository sopVersionRepository;
//
//    @Autowired
//    public SopVersionService(SopVersionRepository sopVersionRepository) {
//        this.sopVersionRepository = sopVersionRepository;
//    }
//
//    // Create a new SOP version
//    public ApiResponse<SopVersionModel> createVersion(SopVersionModel newVersion) {
//        try {
//            // Validate required fields
//            SopVersionUtils.validateRequiredFields(newVersion);
//
//            // Populate default values if needed
//            SopVersionUtils.populateDefaultValues(newVersion);
//
//            // Save the new version
//            SopVersionModel savedVersion = sopVersionRepository.save(newVersion);
//
//            return new ApiResponse<>(
//                    200,
//                    "SOP version created successfully",
//                    savedVersion
//            );
//        } catch (IllegalArgumentException e) {
//            return new ApiResponse<>(
//                    400,
//                    e.getMessage(),
//                    null
//            );
//        } catch (Exception e) {
//            return new ApiResponse<>(
//                    500,
//                    "Error creating SOP version: " + e.getMessage(),
//                    null
//            );
//        }
//    }
//
//    // Get a specific version by ID
//    public ApiResponse<SopVersionModel> getVersionById(String id) {
//        try {
//            Optional<SopVersionModel> version = sopVersionRepository.findById(id);
//            if (version.isPresent()) {
//                return new ApiResponse<>(
//                        200,
//                        "SOP version retrieved successfully",
//                        version.get()
//                );
//            } else {
//                return new ApiResponse<>(
//                        404,
//                        "SOP version not found",
//                        null
//                );
//            }
//        } catch (Exception e) {
//            return new ApiResponse<>(
//                    500,
//                    "Error retrieving SOP version: " + e.getMessage(),
//                    null
//            );
//        }
//    }
//
//    // Get all versions for a specific SOP
//    public ApiResponse<List<SopVersionModel>> getVersionsBySopId(ObjectId sopId) {
//        try {
//            List<SopVersionModel> versions = sopVersionRepository.findBySopId(
//                    sopId,
//                    Sort.by(Sort.Direction.DESC, "versionNumber")
//            );
//
//            return new ApiResponse<>(
//                    200,
//                    "SOP versions retrieved successfully",
//                    versions
//            );
//        } catch (Exception e) {
//            return new ApiResponse<>(
//                    500,
//                    "Error retrieving SOP versions: " + e.getMessage(),
//                    null
//            );
//        }
//    }
//    // Add these methods to your existing SopVersionService class
//
//    public ApiResponse<SopVersionModel> createNewSopVersion(SopVersionModel newVersionDetails, String sopId) {
//        try {
//            // Convert string sopId to ObjectId
//            ObjectId sopObjectId = new ObjectId(sopId);
//            newVersionDetails.setSopId(sopObjectId);
//
//            // Validate and save
//            return createVersion(newVersionDetails);
//        } catch (IllegalArgumentException e) {
//            return new ApiResponse<>(400, "Invalid SOP ID format", null);
//        } catch (Exception e) {
//            return new ApiResponse<>(500, "Error creating SOP version: " + e.getMessage(), null);
//        }
//    }
//
//    public ApiResponse<SopVersionModel> getSopVersionById(String versionId) {
//        return getVersionById(versionId);
//    }
//
//    public ApiResponse<List<SopVersionModel>> getAllVersionsBySopId(String sopId) {
//        try {
//            ObjectId sopObjectId = new ObjectId(sopId);
//            return getVersionsBySopId(sopObjectId);
//        } catch (IllegalArgumentException e) {
//            return new ApiResponse<>(400, "Invalid SOP ID format", null);
//        }
//    }
//
//    public ResponseEntity<ApiResponse<SopVersionModel>> getVersionBySopIdAndVersion(String sopId, String versionNumber) {
//        try {
//            ObjectId sopObjectId = new ObjectId(sopId);
//            Optional<SopVersionModel> versions = sopVersionRepository.findBySopIdAndVersionNumber(sopObjectId, versionNumber);
//
//            if (!versions.isEmpty()) {
//                return ResponseEntity.ok(new ApiResponse<>(200, "Version retrieved successfully", versions.get()));
//            } else {
//                return ResponseEntity.status(404)
//                        .body(new ApiResponse<>(404, "Version not found", null));
//            }
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse<>(400, "Invalid SOP ID format", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body(new ApiResponse<>(500, "Error retrieving version: " + e.getMessage(), null));
//        }
//    }
//
//    // Update a specific version
//    public ApiResponse<SopVersionModel> updateVersion(String id, SopVersionModel updatedVersion) {
//        try {
//            Optional<SopVersionModel> existingVersion = sopVersionRepository.findById(id);
//            if (existingVersion.isPresent()) {
//                SopVersionModel versionToUpdate = existingVersion.get();
//
//                // Update fields if they're not null
//                if (updatedVersion.getTitle() != null) {
//                    versionToUpdate.setTitle(updatedVersion.getTitle());
//                }
//                if (updatedVersion.getDescription() != null) {
//                    versionToUpdate.setDescription(updatedVersion.getDescription());
//                }
//                if (updatedVersion.getVersionNumber() != null) {
//                    versionToUpdate.setVersionNumber(updatedVersion.getVersionNumber());
//                }
//                if (updatedVersion.getCode() != null) {
//                    versionToUpdate.setCode(updatedVersion.getCode());
//                }
//                if (updatedVersion.getVisibility() != null) {
//                    versionToUpdate.setVisibility(updatedVersion.getVisibility());
//                }
//                if (updatedVersion.getCategory() != null) {
//                    versionToUpdate.setCategory(updatedVersion.getCategory());
//                }
//                if (updatedVersion.getImageFile() != null) {
//                    versionToUpdate.setImageFile(updatedVersion.getImageFile());
//                }
//                if (updatedVersion.getDocumentFile() != null) {
//                    versionToUpdate.setDocumentFile(updatedVersion.getDocumentFile());
//                }
//
//                // Save the updated version
//                SopVersionModel savedVersion = sopVersionRepository.save(versionToUpdate);
//
//                return new ApiResponse<>(
//                        200,
//                        "SOP version updated successfully",
//                        savedVersion
//                );
//            } else {
//                return new ApiResponse<>(
//                        404,
//                        "SOP version not found",
//                        null
//                );
//            }
//        } catch (Exception e) {
//            return new ApiResponse<>(
//                    500,
//                    "Error updating SOP version: " + e.getMessage(),
//                    null
//            );
//        }
//    }
//
//    // Delete a specific version
//    public ApiResponse<Void> deleteVersion(String id) {
//        try {
//            if (sopVersionRepository.existsById(id)) {
//                sopVersionRepository.deleteById(id);
//                return new ApiResponse<>(
//                        200,
//                        "SOP version deleted successfully",
//                        null
//                );
//            } else {
//                return new ApiResponse<>(
//                        404,
//                        "SOP version not found",
//                        null
//                );
//            }
//        } catch (Exception e) {
//            return new ApiResponse<>(
//                    500,
//                    "Error deleting SOP version: " + e.getMessage(),
//                    null
//            );
//        }
//    }
//
//    // Get latest version for a specific SOP
//    public ApiResponse<SopVersionModel> getLatestVersion(ObjectId sopId) {
//        try {
//            List<SopVersionModel> versions = sopVersionRepository.findBySopId(
//                    sopId,
//                    Sort.by(Sort.Direction.DESC, "versionNumber")
//            );
//
//            if (!versions.isEmpty()) {
//                return new ApiResponse<>(
//                        200,
//                        "Latest SOP version retrieved successfully",
//                        versions.get(0)
//                );
//            } else {
//                return new ApiResponse<>(
//                        404,
//                        "No versions found for this SOP",
//                        null
//                );
//            }
//        } catch (Exception e) {
//            return new ApiResponse<>(
//                    500,
//                    "Error retrieving latest SOP version: " + e.getMessage(),
//                    null
//            );
//        }
//    }
//}