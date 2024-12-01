package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.dto.SOPInitiationDto;
import com.sop_workflow_service.sop_workflow_service.model.SOPInitiation;
import com.sop_workflow_service.sop_workflow_service.service.SOPInitiationService;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowInitiationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sops")
public class SOPInitiationController {

    @Autowired
    private SOPInitiationService sopInitiationService;

    @Autowired
    private WorkflowInitiationService workflowInitiationService;

    @PostMapping("/initiate")
    public ResponseEntity<SOPInitiation> initiateSOP(@RequestBody SOPInitiationDto sopInitiationDto) {
        SOPInitiation sopInitiation = sopInitiationService.createSOP(sopInitiationDto);
        if (sopInitiationDto.getWorkflowStages() != null) {
            workflowInitiationService.createWorkflowStages(sopInitiation.getId(), sopInitiationDto.getWorkflowStages());
        }
        return ResponseEntity.status(201).body(sopInitiation);
    }

    @GetMapping
    public ResponseEntity<List<SOPInitiation>> getAllSOPs() {
        return ResponseEntity.ok(sopInitiationService.getAllSOPs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SOPInitiation> getSOPById(@PathVariable String id) {
        return ResponseEntity.ok(sopInitiationService.getSOPById(id));
    }

    @GetMapping("/initiated")
    public ResponseEntity<List<SOPInitiation>> getAllInitiatedSOPs() {
        return ResponseEntity.ok(sopInitiationService.getAllInitiatedSOPs());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SOPInitiation> updateSOP(@PathVariable String id, @RequestBody SOPInitiationDto sopInitiationDto) {
        SOPInitiation updatedSOP = sopInitiationService.updateSOP(id, sopInitiationDto);
        return ResponseEntity.ok(updatedSOP);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSOP(@PathVariable String id) {
        sopInitiationService.deleteSOP(id);
        return ResponseEntity.noContent().build();
    }
}
