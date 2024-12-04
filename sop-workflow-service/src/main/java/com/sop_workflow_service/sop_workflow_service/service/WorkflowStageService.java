package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowStageService {

    @Autowired
    private WorkflowStageRepository workflowStageRepository;

    @Autowired
    private SOPRepository sopRepository;

    // Create Workflow Stage (Automatically set to Pending)
    public SOP createStage(WorkflowStage stage) {
        stage.setApprovalStatus("Pending"); // Automatically set to "Pending"
        stage.setCreatedAt(LocalDateTime.now());
        WorkflowStage savedStage = workflowStageRepository.save(stage);

        SOP sop = sopRepository.findById(savedStage.getSopId())
                .orElseThrow(() -> new RuntimeException("SOP not found"));

        if (sop.getWorkflowStages() == null) {
            sop.setWorkflowStages(new ArrayList<>());
        }

        sop.getWorkflowStages().add(savedStage);
        sop.setUpdatedAt(LocalDateTime.now());

        // Transition SOP to "In Review" when stages are added if status is still "Draft"
        if ("Draft".equals(sop.getStatus())) {
            sop.setStatus("In Review");
        }

        sopRepository.save(sop);
        return sop;
    }

    // Update Workflow Stage Status
    public SOP updateStageStatus(String stageId, String action) {
        WorkflowStage stage = workflowStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Workflow stage not found"));

        // Update the stage status based on the action
        switch (action.toLowerCase()) {
            case "review":
                stage.setApprovalStatus("In Review");
                break;
            case "confirm_review":
                stage.setApprovalStatus("Reviewed");
                break;
            case "needs_correction":
                stage.setApprovalStatus("Needs Correction");
                break;
            case "approve":
                stage.setApprovalStatus("Approved");
                break;
            case "reject":
                stage.setApprovalStatus("Rejected");
                break;
            default:
                throw new IllegalArgumentException("Invalid action for status transition");
        }

        // Save the updated stage to the database
        stage.setUpdatedAt(LocalDateTime.now());
        workflowStageRepository.save(stage);

        // Fetch the associated SOP
        SOP sop = sopRepository.findById(stage.getSopId())
                .orElseThrow(() -> new RuntimeException("SOP not found"));

        // If trying to transition the SOP to "In Approval", validate
        if ("approve".equals(action.toLowerCase())) {
            validateTransitionToApproval(sop);
        }

        // Update the SOP status dynamically
        updateSOPStatus(sop);

        return sop;
    }

    // Update SOP Status based on workflow stages
    private void updateSOPStatus(SOP sop) {
        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sop.getSopId());

        // Check if all reviewers have reviewed
        boolean allReviewersReviewed = stages.stream()
                .filter(stage -> "Reviewer".equals(stage.getRoleRequired())) // Only reviewers
                .allMatch(stage -> "Reviewed".equals(stage.getApprovalStatus())); // All reviewers must have reviewed

        // If not all reviewers have reviewed, keep the SOP in "In Review"
        if (!allReviewersReviewed) {
            sop.setStatus("In Review");
            sopRepository.save(sop); // Save the SOP status and return without throwing an exception
            return;
        }

        // Transition SOP to "In Approval" if all reviewers have reviewed
        sop.setStatus("In Approval");

        // Check if all approvers have approved
        boolean allApproved = stages.stream()
                .filter(stage -> "Approver".equals(stage.getRoleRequired())) // Only approvers
                .allMatch(stage -> "Approved".equals(stage.getApprovalStatus())); // All approvers must approve

        boolean anyRejected = stages.stream()
                .filter(stage -> "Approver".equals(stage.getRoleRequired())) // Only approvers
                .anyMatch(stage -> "Rejected".equals(stage.getApprovalStatus())); // Any approver rejects

        if (allApproved) {
            sop.setStatus("Approved");
        } else if (anyRejected) {
            sop.setStatus("Rejected");
        }

        // Save the updated SOP
        sop.setUpdatedAt(LocalDateTime.now());
        sopRepository.save(sop);
    }

    // Validate Transition to 'In Approval'
    private void validateTransitionToApproval(SOP sop) {
        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sop.getSopId());

        boolean allReviewersReviewed = stages.stream()
                .filter(stage -> "Reviewer".equals(stage.getRoleRequired())) // Only reviewers
                .allMatch(stage -> "Reviewed".equals(stage.getApprovalStatus())); // All reviewers must have reviewed

        if (!allReviewersReviewed) {
            throw new IllegalStateException("Cannot transition to 'In Approval': There are still reviewers who have not reviewed.");
        }
    }

    // Get Workflow Stage by ID
    public WorkflowStage getStageById(String stageId) {
        return workflowStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Workflow stage not found"));
    }

    // Get All Workflow Stages
    public List<WorkflowStage> getAllStages() {
        return workflowStageRepository.findAll();
    }

    // Get Workflow Stages by SOP ID
    public List<WorkflowStage> getStagesBySOP(String sopId) {
        return workflowStageRepository.findBySopId(sopId);
    }

    // Delete Workflow Stage
    public void deleteStage(String stageId) {
        WorkflowStage stage = workflowStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Workflow stage not found"));

        SOP sop = sopRepository.findById(stage.getSopId())
                .orElseThrow(() -> new RuntimeException("SOP not found"));

        if (sop.getWorkflowStages() != null) {
            sop.getWorkflowStages().removeIf(existingStage -> existingStage.getStageId().equals(stageId));
        }

        sop.setUpdatedAt(LocalDateTime.now());
        sopRepository.save(sop);

        workflowStageRepository.deleteById(stageId);
    }
}
