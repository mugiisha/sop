package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.model.Reviewer;
import com.sop_workflow_service.sop_workflow_service.repository.ReviewerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewerService {

    @Autowired
    private ReviewerRepository reviewerRepository;

    public Reviewer createReviewer(Reviewer reviewer) {
        return reviewerRepository.save(reviewer);
    }

    public List<Reviewer> getAllReviewers() {
        return reviewerRepository.findAll();
    }

    public Reviewer getReviewerById(String id) {
        return reviewerRepository.findById(id).orElseThrow(() -> new RuntimeException("Reviewer not found with ID: " + id));
    }

    public Reviewer updateReviewer(String id, Reviewer updatedReviewer) {
        Reviewer existingReviewer = reviewerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reviewer not found with ID: " + id));

        existingReviewer.setName(updatedReviewer.getName());
        existingReviewer.setEmail(updatedReviewer.getEmail());

        return reviewerRepository.save(existingReviewer);
    }

    public void deleteReviewer(String id) {
        if (!reviewerRepository.existsById(id)) {
            throw new RuntimeException("Reviewer not found with ID: " + id);
        }
        reviewerRepository.deleteById(id);
    }
}
