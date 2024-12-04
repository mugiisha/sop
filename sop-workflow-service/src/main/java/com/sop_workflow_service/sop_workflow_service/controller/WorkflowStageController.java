package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-stages")
public class WorkflowStageController {

    @Autowired
    private WorkflowStageService workflowStageService;

    @PostMapping
    public SOP createStage(@RequestBody WorkflowStage stage) {
        return workflowStageService.createStage(stage);
    }

    @GetMapping("/{stageId}")
    public WorkflowStage getStageById(@PathVariable String stageId) {
        return workflowStageService.getStageById(stageId);
    }

    @GetMapping
    public List<WorkflowStage> getAllStages() {
        return workflowStageService.getAllStages();
    }

    @GetMapping("/sop/{sopId}")
    public List<WorkflowStage> getStagesBySOP(@PathVariable String sopId) {
        return workflowStageService.getStagesBySOP(sopId);
    }

    @PutMapping("/{stageId}/action/{action}")
    public SOP updateStageStatus(@PathVariable String stageId, @PathVariable String action) {
        return workflowStageService.updateStageStatus(stageId, action);
    }

    @DeleteMapping("/{stageId}")
    public void deleteStage(@PathVariable String stageId) {
        workflowStageService.deleteStage(stageId);
    }
}
