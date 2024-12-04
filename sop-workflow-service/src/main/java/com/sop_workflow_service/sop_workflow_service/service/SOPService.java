package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SOPService {

    @Autowired
    private SOPRepository sopRepository;

    @Autowired
    private WorkflowStageRepository workflowStageRepository;

    // Create SOP
    public SOP createSOP(SOP sop) {
        sop.setStatus("Draft");
        sop.setCreatedAt(java.time.LocalDateTime.now());
        sop.setUpdatedAt(java.time.LocalDateTime.now());
        return sopRepository.save(sop);
    }

    // Get SOP by ID with updated Workflow Stages
    public SOP getSOP(String id) {
        SOP sop = sopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SOP not found"));

        // Fetch updated workflow stages
        List<WorkflowStage> stages = workflowStageRepository.findBySopId(id);
        sop.setWorkflowStages(stages);

        // Dynamically determine the status if necessary
        updateDynamicSOPStatus(sop, stages);

        return sop;
    }

    // Get All SOPs
    public List<SOP> getAllSOPs() {
        List<SOP> sops = sopRepository.findAll();
        for (SOP sop : sops) {
            List<WorkflowStage> stages = workflowStageRepository.findBySopId(sop.getSopId());
            sop.setWorkflowStages(stages);
            updateDynamicSOPStatus(sop, stages);
        }
        return sops;
    }

    // Update SOP
    public SOP updateSOP(SOP sop) {
        sop.setUpdatedAt(java.time.LocalDateTime.now());
        return sopRepository.save(sop);
    }

    // Delete SOP
    public void deleteSOP(String id) {
        sopRepository.deleteById(id);
    }

    // Update SOP Status Dynamically Based on Workflow Stages
    private void updateDynamicSOPStatus(SOP sop, List<WorkflowStage> stages) {
        boolean allReviewersReviewed = stages.stream()
                .filter(stage -> "Reviewer".equals(stage.getRoleRequired()))
                .allMatch(stage -> "Reviewed".equals(stage.getApprovalStatus()));

        boolean allApproversApproved = stages.stream()
                .filter(stage -> "Approver".equals(stage.getRoleRequired()))
                .allMatch(stage -> "Approved".equals(stage.getApprovalStatus()));

        boolean anyRejected = stages.stream()
                .anyMatch(stage -> "Rejected".equals(stage.getApprovalStatus()));

        if (anyRejected) {
            sop.setStatus("Rejected");
        } else if (allApproversApproved) {
            sop.setStatus("Approved");
        } else if (allReviewersReviewed) {
            sop.setStatus("In Approval");
        } else {
            sop.setStatus("In Review");
        }

        sop.setUpdatedAt(java.time.LocalDateTime.now());
        sopRepository.save(sop);
    }
}
