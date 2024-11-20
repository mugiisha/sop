package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.model.Reviewer;
import com.sop_workflow_service.sop_workflow_service.service.ReviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    @Autowired
    private ReviewerService reviewerService;

    @PostMapping
    public ResponseEntity<Reviewer> createReviewer(@RequestBody Reviewer reviewer) {
        Reviewer createdReviewer = reviewerService.createReviewer(reviewer);
        return ResponseEntity.status(201).body(createdReviewer);
    }

    @GetMapping
    public ResponseEntity<List<Reviewer>> getAllReviewers() {
        return ResponseEntity.ok(reviewerService.getAllReviewers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reviewer> getReviewerById(@PathVariable String id) {
        Reviewer reviewer = reviewerService.getReviewerById(id);
        return ResponseEntity.ok(reviewer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reviewer> updateReviewer(@PathVariable String id, @RequestBody Reviewer updatedReviewer) {
        Reviewer reviewer = reviewerService.updateReviewer(id, updatedReviewer);
        return ResponseEntity.ok(reviewer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewer(@PathVariable String id) {
        reviewerService.deleteReviewer(id);
        return ResponseEntity.noContent().build();
    }
}
