package com.sop_content_service.sop_content_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.exception.SopException;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import com.sop_content_service.sop_content_service.util.SopModelUtils;
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

    @Autowired
    private EmailService emailService;

    public ResponseEntity<ApiResponse<SopModel>> createSOP(
            String sopId, SopModel sopModel, MultipartFile imageFile, MultipartFile documentFile) {
        try {
            SopModel existingSop = sopRepository.findById(sopId)
                    .orElseThrow(() -> new SopException.SopNotFoundException("SOP with ID " + sopId + " does not exist."));

            // Use utility class to update fields
            SopModelUtils.updateTitle(existingSop, sopModel);
            SopModelUtils.updateDescription(existingSop, sopModel);
            SopModelUtils.updateNewSection(existingSop, sopModel);
            SopModelUtils.updateCode(existingSop, sopModel);
            SopModelUtils.updateVisibility(existingSop, sopModel);
            SopModelUtils.updateAuthors(existingSop, sopModel);
            SopModelUtils.updateReviewers(existingSop, sopModel);
            SopModelUtils.updateApprovers(existingSop, sopModel);

            // Upload image and document if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = uploadImageToCloudinary(imageFile);
                existingSop.setImageUrl(imageUrl);
            }

            if (documentFile != null && !documentFile.isEmpty()) {
                String documentUrl = uploadDocumentToCloudinary(documentFile);
                existingSop.setDocumentUrl(documentUrl);
            }

            // Save and return updated SOP
            SopModel updatedSop = sopRepository.save(existingSop);

            System.out.println("updated sop"+updatedSop);

            // Notify reviewers about the newly created SOP
            notifyReviewers(updatedSop);

            ApiResponse<SopModel> response = new ApiResponse<>("SOP Draft updated successfully", updatedSop);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            throw new RuntimeException("An error occurred while processing files.", e);
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

    // Notify reviewers via email
    private void notifyReviewers(SopModel sop) {
        String subject = "SOP Created: Review Required for " + sop.getTitle();
        String message = generateReviewerEmailContent(sop);

        // Loop through reviewers and send emails
        for (String reviewerEmail : sop.getReviewers()) {
            emailService.sendEmail(reviewerEmail, subject, message);
        }
    }

    // Generate email content for reviewers
    private String generateReviewerEmailContent(SopModel sop) {
        return new StringBuilder()
                .append("Dear Reviewer,\n\n")
                .append("A new Standard Operating Procedure (SOP) has been created and requires your review.\n\n")
                .append("Here are the details:\n")
                .append("Title: ").append(sop.getTitle()).append("\n")
                .append("Category: ").append(sop.getCategory()).append("\n")
                .append("Visibility: ").append(sop.getVisibility()).append("\n")
                .append("Version: ").append(sop.getVersion()).append("\n")
                .append("Status: ").append(sop.getStatus()).append("\n\n")
                .append("Please review the SOP and leave your comments at your earliest convenience.\n\n")
                .append("Best regards,\n")
                .append("Staff")
                .toString();
    }
}