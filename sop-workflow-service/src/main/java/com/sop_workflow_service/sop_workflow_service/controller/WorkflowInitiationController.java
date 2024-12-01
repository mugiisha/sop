package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.dto.WorkflowInitiationDto;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowInitiation;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowInitiationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-initiations")
public class WorkflowInitiationController {

    @Autowired
    private WorkflowInitiationService workflowInitiationService;

    // Get all WorkflowInitiations
    @GetMapping
    public ResponseEntity<List<WorkflowInitiation>> getAllWorkflowInitiations() {
        return ResponseEntity.ok(workflowInitiationService.getAllWorkflowStages());
    }

    // Get WorkflowInitiations by SOP ID
    @GetMapping("/{sopId}")
    public ResponseEntity<List<WorkflowInitiation>> getWorkflowInitiationsBySopId(@PathVariable String sopId) {
        return ResponseEntity.ok(workflowInitiationService.getWorkflowStagesBySopId(sopId));
    }

    // Update WorkflowInitiation
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowInitiation> updateWorkflowInitiation(
            @PathVariable String id,
            @RequestBody WorkflowInitiationDto workflowInitiationDto) {
        WorkflowInitiation updatedStage = workflowInitiationService.updateWorkflowStage(id, workflowInitiationDto);
        return ResponseEntity.ok(updatedStage);
    }

    // Delete WorkflowInitiation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflowInitiation(@PathVariable String id) {
        workflowInitiationService.deleteWorkflowStage(id);
        return ResponseEntity.noContent().build();
    }
}
