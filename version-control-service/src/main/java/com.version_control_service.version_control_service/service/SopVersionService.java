package com.version_control_service.version_control_service.service;

import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.exception.SopNotFoundException;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.repository.SopVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SopVersionService {

    @Autowired
    private SopVersionRepository sopVersionRepository;

    // Initialize SOP version and save to database
    public ApiResponse<SopVersionModel> initializeSopVersion(SopVersionModel sopVersionModel) {
        try {
            SopVersionModel createdSopVersion = sopVersionRepository.save(sopVersionModel);
            return new ApiResponse<>(200, "SOP Version Initialized successfully", createdSopVersion);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Failed to initialize SOP Version: " + e.getMessage(), null);
        }
    }

    // Fetch SOP versions by SOP ID
    public ApiResponse<List<SopVersionModel>> getVersionsBySopId(String sopId) {
        List<SopVersionModel> versions = sopVersionRepository.findBySopId(sopId);
        if (versions.isEmpty()) {
            throw new SopNotFoundException("No versions found for SOP ID: " + sopId);
        }
        return new ApiResponse<>(200, "Fetched SOP versions successfully", versions);
    }

    // Fetch the latest SOP version by SOP ID
    public ApiResponse<SopVersionModel> getLatestVersion(String sopId) {
        SopVersionModel latestVersion = sopVersionRepository.findTopBySopIdOrderByCreatedAtDesc(sopId);
        if (latestVersion == null) {
            throw new SopNotFoundException("No versions found for SOP ID: " + sopId);
        }
        return new ApiResponse<>(200, "Fetched latest SOP version successfully", latestVersion);
    }

    // Fetch a specific version of SOP by SOP ID and version number
    public ApiResponse<SopVersionModel> getSpecificVersion(String sopId, String versionNumber) {
        SopVersionModel specificVersion = sopVersionRepository.findBySopIdAndVersionNumber(sopId, versionNumber);
        if (specificVersion == null) {
            throw new SopNotFoundException("No version found for SOP ID: " + sopId + " and Version: " + versionNumber);
        }
        return new ApiResponse<>(200, "Fetched specific SOP version successfully", specificVersion);
    }
}
