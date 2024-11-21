package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.dto.ApiResponse;
import com.sop_content_service.sop_content_service.exception.SopException;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SopInitializeService {

    @Autowired
    private SopRepository sopRepository;

    @Autowired
    private EmailService emailService;

    // Method to initialize a new SOP
    public ResponseEntity<ApiResponse<SopModel>> initializeSOP(SopModel sopModel) {
        try {
            // Create a new SopModel instance with data from SopInitializationModel
            SopModel sop = new SopModel(
                    sopModel.getTitle(),
                    sopModel.getVisibility(),
                    sopModel.getAuthors(),
                    sopModel.getReviewers(),
                    sopModel.getApprovers(),
                    sopModel.getCategory()
            );

            // Save the SOP initialization to the database
            SopModel createdSop = sopRepository.save(sop);  // Save the newly created SopModel object

            // Send emails to all authors
            notifyAuthors(createdSop);

            // Return the created SOP wrapped in ApiResponse with HttpStatus.CREATED (201)
            ApiResponse<SopModel> response = new ApiResponse<>("SOP Initialized successfully", createdSop);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SOP");
        }
    }

    // Notify authors via email
    private void notifyAuthors(SopModel sop) {
        String subject = "New SOP Initialized: " + sop.getTitle();
        String message = generateEmailContent(sop);

        // Loop through authors and send emails
        for (String authorEmail : sop.getAuthors()) {
            emailService.sendEmail(authorEmail, subject, message);
        }
    }

    // Generate email content for notification
    private String generateEmailContent(SopModel sop) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Author,\n\n")
                .append("A new Standard Operating Procedure (SOP) has been initialized.\n\n")
                .append("Here are the details:\n")
                .append("Title: ").append(sop.getTitle()).append("\n")
                .append("Category: ").append(sop.getCategory()).append("\n")
                .append("Visibility: ").append(sop.getVisibility()).append("\n")
                .append("Version: ").append(sop.getVersion()).append("\n")
                .append("Status: ").append(sop.getStatus()).append("\n\n")
                .append("Reviewers: ").append(String.join(", ", sop.getReviewers())).append("\n")
                .append("Approvers: ").append(String.join(", ", sop.getApprovers())).append("\n\n")
                .append("Please take action and create this SOP at your earliest convenience.\n\n")
                .append("Best regards,\n")
                .append("Staff");

        return content.toString();
    }
}
