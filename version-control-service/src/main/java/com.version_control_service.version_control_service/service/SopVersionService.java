package com.version_control_service.version_control_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.version_control_service.version_control_service.config.CloudinaryConfig;
import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.dto.SopDto;
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
import java.util.NoSuchElementException;
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

    public SopVersionModel createNewSopVersion(SopDto newVersionDetails, String sopId) {
        SopVersionModel newVersion = new SopVersionModel();
        newVersion.setSopId(new ObjectId(sopId)); // Convert sopId to ObjectId
        newVersion.setTitle(newVersionDetails.getTitle());
        newVersion.setDescription(newVersionDetails.getDescription());
        newVersion.setVersionNumber(newVersionDetails.getVersion());
        newVersion.setCreatedBy("admin"); // Example: replace with the actual user
        newVersion.setCode("unique-code"); // Generate or pass a unique code
        newVersion.setVisibility("Public");
        newVersion.setCategory("General");
        newVersion.setImageFile(newVersionDetails.getImageUrl());
        newVersion.setDocumentFile(newVersionDetails.getDocumentUrl());
        return sopVersionRepository.save(newVersion);
    }

    // Retrieve a specific SOP version by version ID
    public SopVersionModel getSopVersionById(String versionId) {
        return sopVersionRepository.findById(versionId)
                .orElseThrow(() -> new NoSuchElementException("SOP Version not found for ID: " + versionId));
    }

    // Retrieve all SOP versions associated with a specific SOP ID
    public List<SopVersionModel> getAllVersionsBySopId(String sopId) {
        return sopVersionRepository.findBySopId(new ObjectId(sopId));
    }

}

