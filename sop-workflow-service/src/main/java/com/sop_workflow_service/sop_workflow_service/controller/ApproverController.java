package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.model.Approver;
import com.sop_workflow_service.sop_workflow_service.service.ApproverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvers")
public class ApproverController {

    @Autowired
    private ApproverService approverService;

    @PostMapping
    public ResponseEntity<Approver> createApprover(@RequestBody Approver approver) {
        Approver createdApprover = approverService.createApprover(approver);
        return ResponseEntity.status(201).body(createdApprover);
    }

    @GetMapping
    public ResponseEntity<List<Approver>> getAllApprovers() {
        return ResponseEntity.ok(approverService.getAllApprovers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Approver> getApproverById(@PathVariable String id) {
        Approver approver = approverService.getApproverById(id);
        return ResponseEntity.ok(approver);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Approver> updateApprover(@PathVariable String id, @RequestBody Approver updatedApprover) {
        Approver approver = approverService.updateApprover(id, updatedApprover);
        return ResponseEntity.ok(approver);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprover(@PathVariable String id) {
        approverService.deleteApprover(id);
        return ResponseEntity.noContent().build();
    }
}

