package com.sop_content_service.sop_content_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.exception.SopException;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class SopService {

    @Autowired
    private SopRepository sopRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private Cloudinary cloudinary;

    public ResponseEntity<ApiResponse<SopModel>> createSOP(SopModel sopModel, MultipartFile imageFile, MultipartFile documentFile) {
        try {
            // Upload image and set URL if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = uploadImageToCloudinary(imageFile);
                sopModel.setImageUrl(imageUrl);
            }

            // Upload document and set URL if provided
            if (documentFile != null && !documentFile.isEmpty()) {
                String documentUrl = uploadDocumentToCloudinary(documentFile);
                sopModel.setDocumentUrl(documentUrl);
            }

            // Save SOP to the database
            SopModel createdSop = sopRepository.save(sopModel);

            // Return the created SOP wrapped in ApiResponse with HttpStatus.CREATED (201)
            ApiResponse<SopModel> response = new ApiResponse<>("SOP Draft created successfully", createdSop);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IOException e) {
            // Handle IOException during file upload
            if (imageFile != null && !imageFile.isEmpty()) {
                throw new SopException.ImageUploadException("Failed to upload image");
            } else if (documentFile != null && !documentFile.isEmpty()) {
                throw new SopException.DocumentUploadException("Failed to upload document");
            } else {
                throw new RuntimeException("An unknown error occurred while processing the files.");
            }
        }
    }

    private String uploadImageToCloudinary(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url").toString();
    }

    private String uploadDocumentToCloudinary(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url").toString();
    }

    public ResponseEntity<ApiResponse<List<SopModel>>> getAllSOPs() {
        // Fetch all SOPs ordered by createdAt descending
        List<SopModel> sopList = sopRepository.findAllByOrderByCreatedAtDesc();

        // Wrap the result in an ApiResponse
        ApiResponse<List<SopModel>> response = new ApiResponse<>("Fetched all SOPs successfully", sopList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<ApiResponse<List<SopModel>>> getSOPsByStatus(String status) {
        // Fetch SOPs with the specified status, ordered by creation date descending
        List<SopModel> sopList = sopRepository.findByStatusOrderByCreatedAtDesc(status);

        // Check if any SOPs were found
        if (sopList.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse<>("No SOPs found with status: " + status, null),
                    HttpStatus.NOT_FOUND
            );
        }

        // Wrap the result in an ApiResponse
        ApiResponse<List<SopModel>> response = new ApiResponse<>("Fetched SOPs with status: " + status, sopList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
