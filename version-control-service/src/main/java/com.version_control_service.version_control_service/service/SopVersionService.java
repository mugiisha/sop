package com.version_control_service.version_control_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.version_control_service.version_control_service.config.CloudinaryConfig;
import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.exception.SopNotFoundException;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.repository.SopVersionRepository;
import com.version_control_service.version_control_service.utils.SopVersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

@Service
public class SopVersionService {

    @Autowired
    private SopVersionRepository sopVersionRepository;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private Cloudinary cloudinary;

    private static final Logger logger = LoggerFactory.getLogger(SopVersionService.class);

    public ApiResponse<SopVersionModel> initializeSopVersion(
            String sopId, SopVersionModel sopVersionModel,
            MultipartFile imageFile, MultipartFile documentFile) {

        logger.info("Initializing SOP version for SOP ID: {}", sopId);

        try {
            // Log incoming data
            logger.debug("Request data - sopId: {}", sopId);
            logger.debug("Request data - sopVersionModel: {}", sopVersionModel);
            if (imageFile != null) {
                logger.debug("Request includes an image file: {}", imageFile.getOriginalFilename());
            } else {
                logger.debug("No image file provided in the request.");
            }
            if (documentFile != null) {
                logger.debug("Request includes a document file: {}", documentFile.getOriginalFilename());
            } else {
                logger.debug("No document file provided in the request.");
            }

            // Convert sopId to ObjectId
            ObjectId objectId = new ObjectId(sopId);
            logger.debug("Converted sopId to ObjectId: {}", objectId);

            // Check if SOP exists
            logger.debug("Looking up SOP with ObjectId: {}", objectId);
            SopVersionModel existingSop = sopVersionRepository.findById(sopId)
                    .orElseThrow(() -> new SopNotFoundException("SOP with ID " + sopId + " does not exist."));

            logger.debug("SOP found: {}", existingSop);

            // Use SopVersionUtils to set sopId and update other fields
            SopVersionUtils.setSopId(existingSop, sopVersionModel);

            // Upload image and document if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.debug("Uploading image file for SOP ID: {}", sopId);
                String imageUrl = uploadImageToCloudinary(imageFile);
                existingSop.setImageFile(imageUrl);
                logger.debug("Image uploaded successfully: {}", imageUrl);
            }

            if (documentFile != null && !documentFile.isEmpty()) {
                logger.debug("Uploading document file for SOP ID: {}", sopId);
                String documentUrl = uploadDocumentToCloudinary(documentFile);
                existingSop.setDocumentFile(documentUrl);
                logger.debug("Document uploaded successfully: {}", documentUrl);
            }

            // Save the updated SOP version
            SopVersionModel createdSopVersion = sopVersionRepository.save(existingSop);
            logger.info("SOP version initialized successfully for SOP ID: {}", sopId);

            return new ApiResponse<>(200, "SOP Version Initialized successfully", createdSopVersion);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid SOP ID format: {}", sopId, e);
            return new ApiResponse<>(400, "Invalid SOP ID format: " + sopId, null);
        } catch (SopNotFoundException e) {
            logger.error("SOP not found: {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Failed to initialize SOP version for SOP ID: {}. Error: {}", sopId, e.getMessage(), e);
            return new ApiResponse<>(500, "Failed to initialize SOP Version: " + e.getMessage(), null);
        }
    }



    private String uploadImageToCloudinary(MultipartFile file) throws IOException {
        logger.debug("Uploading image to Cloudinary");
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("url").toString();
        logger.debug("Image uploaded to Cloudinary: {}", imageUrl);
        return imageUrl;
    }

    private String uploadDocumentToCloudinary(MultipartFile file) throws IOException {
        logger.debug("Uploading document to Cloudinary");
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String documentUrl = uploadResult.get("url").toString();
        logger.debug("Document uploaded to Cloudinary: {}", documentUrl);
        return documentUrl;
    }

    // Fetch SOP versions by SOP ID
    public ApiResponse<List<SopVersionModel>> getVersionsBySopId(String sopId) {
        ObjectId objectId = new ObjectId(sopId);
        List<SopVersionModel> versions = sopVersionRepository.findBySopId(objectId);
        if (versions.isEmpty()) {
            throw new SopNotFoundException("No versions found for SOP ID: " + sopId);
        }
        return new ApiResponse<>(200, "Fetched SOP versions successfully", versions);
    }

//     Fetch the latest SOP version by SOP ID
    public ApiResponse<SopVersionModel> getLatestVersion(String sopId) {
        ObjectId objectId = new ObjectId(sopId);
        SopVersionModel latestVersion = sopVersionRepository.findTopBySopIdOrderByCreatedAtDesc(objectId);
        if (latestVersion == null) {
            throw new SopNotFoundException("No versions found for SOP ID: " + sopId);
        }
        return new ApiResponse<>(200, "Fetched latest SOP version successfully", latestVersion);
    }

    // Fetch a specific version of SOP by SOP ID and version number
    public ApiResponse<SopVersionModel> getSpecificVersion(String sopId, String versionNumber) {
        ObjectId objectId = new ObjectId(sopId);
        SopVersionModel specificVersion = sopVersionRepository.findBySopIdAndVersionNumber(objectId, versionNumber);
        if (specificVersion == null) {
            throw new SopNotFoundException("No version found for SOP ID: " + sopId + " and Version: " + versionNumber);
        }
        return new ApiResponse<>(200, "Fetched specific SOP version successfully", specificVersion);
    }

    // Fetch a specific SOP version by version ID (using String as versionId)
//    public ApiResponse<List<SopVersionModel>> getVersionById(String versionId) {
//        // Fetch the SOP version(s) by versionId (returning a List)
//        List<SopVersionModel> versions = sopVersionRepository.findBySopIds(versionId);
//
//        // If no version(s) are found, throw a custom exception or return a suitable response
//        if (versions.isEmpty()) {
//            throw new SopNotFoundException("SOP Version(s) not found for ID: " + versionId);
//        }
//
//        // Return a successful response
//        return new ApiResponse<>(200, "Fetched SOP Version(s) successfully", versions);
//    }

}

