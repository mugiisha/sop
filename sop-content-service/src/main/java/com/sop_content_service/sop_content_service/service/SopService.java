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
import java.lang.reflect.Field;

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

    public ResponseEntity<ApiResponse<SopModel>> createSOP(
            String sopId, SopModel sopModel, MultipartFile imageFile, MultipartFile documentFile) {
        try {
            // Check if the provided SOP ID exists in the database
            SopModel existingSop = sopRepository.findById(sopId)
                    .orElseThrow(() -> new SopException.SopNotFoundException("SOP with ID " + sopId + " does not exist."));

            // Update fields from the request, keeping existing values for null fields
            if (sopModel.getTitle() != null) existingSop.setTitle(sopModel.getTitle());
            if (sopModel.getDescription() != null) existingSop.setDescription(sopModel.getDescription());
            if (sopModel.getNewSection() != null) existingSop.setNewSection(sopModel.getNewSection());
            if (sopModel.getCode() != null) existingSop.setCode(sopModel.getCode());
            if (sopModel.getVisibility() != null) existingSop.setVisibility(sopModel.getVisibility());
            if (sopModel.getAuthor() != null) existingSop.setAuthor(sopModel.getAuthor());
            if (sopModel.getReviewer() != null) existingSop.setReviewer(sopModel.getReviewer());
            if (sopModel.getApprover() != null) existingSop.setApprover(sopModel.getApprover());

            // Upload and update image URL if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = uploadImageToCloudinary(imageFile);
                existingSop.setImageUrl(imageUrl);
            }

            // Upload and update document URL if provided
            if (documentFile != null && !documentFile.isEmpty()) {
                String documentUrl = uploadDocumentToCloudinary(documentFile);
                existingSop.setDocumentUrl(documentUrl);
            }

            // Save the updated SOP to the database
            SopModel updatedSop = sopRepository.save(existingSop);

            // Return the updated SOP wrapped in ApiResponse with HttpStatus.OK (200)
            ApiResponse<SopModel> response = new ApiResponse<>("SOP Draft updated successfully", updatedSop);
            return new ResponseEntity<>(response, HttpStatus.OK);

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
