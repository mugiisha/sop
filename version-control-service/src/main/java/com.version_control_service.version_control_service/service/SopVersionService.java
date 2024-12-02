package com.version_control_service.version_control_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.version_control_service.version_control_service.config.CloudinaryConfig;
import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.dto.SopDto;
import com.version_control_service.version_control_service.exception.SopNotFoundException;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.repository.SopVersionRepository;
import com.version_control_service.version_control_service.utils.ValidateSopVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.web.server.ResponseStatusException;

import static com.version_control_service.version_control_service.utils.ValidateSopVersion.validateSopVersion;

@Service
public class SopVersionService {

    @Autowired
    private SopVersionRepository sopVersionRepository;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private Cloudinary cloudinary;

    private static final Logger logger = LoggerFactory.getLogger(SopVersionService.class);

    public SopVersionModel createNewSopVersion(SopDto newVersionDetails, String sopId) {
        ObjectId objectId = new ObjectId(sopId);
        if (sopVersionRepository.findBySopId(objectId) == null ||
                sopVersionRepository.findBySopId(objectId).isEmpty()) {
            throw new IllegalArgumentException("Invalid SOP ID: " + sopId);
        }
        SopVersionModel newVersion = new SopVersionModel();
        newVersion.setSopId(objectId); // Convert sopId to ObjectId
        newVersion.setTitle(newVersionDetails.getTitle());
        newVersion.setDescription(newVersionDetails.getDescription());
        newVersion.setVersionNumber(newVersionDetails.getVersion());
        newVersion.setCreatedBy("HOD");
        // Assign default or calculated values where applicable
        newVersion.setCode(newVersionDetails.getCode() != null ? newVersionDetails.getCode() : "ABC123");
        newVersion.setVisibility(newVersionDetails.getVisibility() != null ? newVersionDetails.getVisibility() : "Public");
        newVersion.setCategory(newVersionDetails.getCategory() != null ? newVersionDetails.getCategory() : "General");
        newVersion.setImageFile(newVersionDetails.getImageUrl());
        newVersion.setDocumentFile(newVersionDetails.getDocumentUrl());
        return sopVersionRepository.save(newVersion);
    }


    public ApiResponse<SopVersionModel> getSopVersionById(String versionId) {
        try {
            SopVersionModel sopVersion = sopVersionRepository.findById(versionId)
                    .orElseThrow(() -> new NoSuchElementException("SOP Version not found for ID: " + versionId));

            String message = String.format("Successfully retrieved SOP version for ID: %s", versionId);
            logger.info(message);
            return new ApiResponse<>(HttpStatus.OK.value(), message, sopVersion);

        } catch (NoSuchElementException e) {
            String errorMessage = e.getMessage();
            logger.warn(errorMessage);
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), errorMessage, null);

        } catch (Exception e) {
            String errorMessage = String.format("Error occurred while retrieving SOP version for ID: %s", e.getMessage());
            logger.error(errorMessage, e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, null);
        }
    }

    public ApiResponse<List<SopVersionModel>> getAllVersionsBySopId(String sopId) {
        try {
            List<SopVersionModel> sopVersions = sopVersionRepository.findBySopId(new ObjectId(sopId));

            if (sopVersions.isEmpty()) {
                String message = String.format("No SOP versions found for sopId: %s", sopId);
                logger.warn(message);
                return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), message, null);
            }

            String message = String.format("Successfully retrieved SOP versions for sopId: %s", sopId);
            logger.info(message);
            return new ApiResponse<>(HttpStatus.OK.value(), message, sopVersions);

        } catch (Exception e) {
            String errorMessage = String.format("Error occurred while retrieving SOP versions: %s", e.getMessage());
            logger.error(errorMessage, e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, null);
        }
    }

    public ResponseEntity<ApiResponse<SopVersionModel>> getVersionBySopIdAndVersion(String sopId, String versionNumber) {
        logger.info("Fetching SOP version for sopId: {} and versionNumber: {}", sopId, versionNumber);

        try {
            // Fetch versions by sopId
            List<SopVersionModel> sopVersions = sopVersionRepository.findBySopId(new ObjectId(sopId));
            logger.debug("Fetched {} versions for sopId: {}", sopVersions.size(), sopId);

            if (sopVersions.isEmpty()) {
                logger.warn("No SOP versions found for sopId: {}", sopId);
                return new ResponseEntity<>(
                        new ApiResponse<>(HttpStatus.NOT_FOUND.value(),
                                String.format("No SOP versions found for sopId: %s", sopId),
                                null),
                        HttpStatus.NOT_FOUND
                );
            }

            // Normalize input version number
            String normalizedInputVersion = versionNumber.trim().replaceAll("\"", "");

            for (SopVersionModel version : sopVersions) {
                String normalizedDataVersion = version.getVersionNumber().trim();

                if (normalizedDataVersion.equals(normalizedInputVersion) ||
                        normalizedDataVersion.startsWith(normalizedInputVersion + ".")) {
                    logger.info("Found matching version: {} for sopId: {}", normalizedDataVersion, sopId);
                    return new ResponseEntity<>(
                            new ApiResponse<>(HttpStatus.OK.value(), "SOP version found", version),
                            HttpStatus.OK
                    );
                }
            }

            logger.warn("No matching SOP version found for sopId: {} and versionNumber: {}", sopId, versionNumber);
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.NOT_FOUND.value(),
                            String.format("No SOP version found for sopId: %s and versionNumber: %s", sopId, versionNumber),
                            null),
                    HttpStatus.NOT_FOUND
            );

        } catch (Exception e) {
            logger.error("Error retrieving SOP version for sopId: {} and versionNumber: {}", sopId, versionNumber, e);
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Error retrieving SOP version: %s", e.getMessage()),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }







}

