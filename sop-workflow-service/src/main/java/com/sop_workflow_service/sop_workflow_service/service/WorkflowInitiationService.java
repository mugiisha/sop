package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.dto.WorkflowInitiationDto;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowInitiation;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowInitiationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowInitiationService {

    @Autowired
    private WorkflowInitiationRepository workflowInitiationRepository;

    // Get all WorkflowInitiations
    public List<WorkflowInitiation> getAllWorkflowStages() {
        return workflowInitiationRepository.findAll();
    }

    // Get WorkflowInitiations by SOP ID
    public List<WorkflowInitiation> getWorkflowStagesBySopId(String sopId) {
        return workflowInitiationRepository.findBySopId(sopId);
    }

    // Create WorkflowInitiations for a given SOP
    public List<WorkflowInitiation> createWorkflowStages(String sopId, List<WorkflowInitiationDto> workflowStageRequests) {
        List<WorkflowInitiation> stages = new ArrayList<>();
        for (WorkflowInitiationDto stageRequest : workflowStageRequests) {
            WorkflowInitiation stage = new WorkflowInitiation();
            stage.setSopId(sopId);
            stage.setName(stageRequest.getName());
            stage.setRoleRequired(stageRequest.getRoleRequired());
            stage.setAssignedUser(stageRequest.getAssignedUser());
            stage.setApprovalStatus("PENDING");
            stage.setSequenceNumber(stageRequest.getSequenceNumber());
            stage.setCreatedAt(LocalDateTime.now());
            stage.setUpdatedAt(LocalDateTime.now());
            stages.add(stage);
        }
        return workflowInitiationRepository.saveAll(stages);
    }

    // Update WorkflowInitiation
    public WorkflowInitiation updateWorkflowStage(String id, WorkflowInitiationDto workflowInitiationDto) {
        WorkflowInitiation stage = workflowInitiationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkflowInitiation not found with ID: " + id));
        stage.setName(workflowInitiationDto.getName());
        stage.setRoleRequired(workflowInitiationDto.getRoleRequired());
        stage.setAssignedUser(workflowInitiationDto.getAssignedUser());
        stage.setApprovalStatus(workflowInitiationDto.getApprovalStatus());
        stage.setSequenceNumber(workflowInitiationDto.getSequenceNumber());
        stage.setUpdatedAt(LocalDateTime.now());
        return workflowInitiationRepository.save(stage);
    }

    // Delete WorkflowInitiation
    public void deleteWorkflowStage(String id) {
        workflowInitiationRepository.deleteById(id);
    }
}
