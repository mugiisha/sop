package com.sop_workflow_service.sop_workflow_service.controller;

import com.sop_workflow_service.sop_workflow_service.dto.SOPRequestDto;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.service.SOPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sops")
public class SOPController {

    @Autowired
    private SOPService sopService;

    @PostMapping
    public ResponseEntity<SOP> createSOP(@RequestBody SOPRequestDto sopRequestDto) {
        SOP sop = sopService.createSOP(sopRequestDto);
        return ResponseEntity.status(201).body(sop);
    }

    @GetMapping
    public ResponseEntity<List<SOP>> getAllSOPs() {
        return ResponseEntity.ok(sopService.getAllSOPs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SOP> getSOPById(@PathVariable String id) {
        SOP sop = sopService.getSOPById(id);
        return ResponseEntity.ok(sop);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SOP> updateSOP(@PathVariable String id, @RequestBody SOPRequestDto sopRequestDto) {
        SOP updatedSOP = sopService.updateSOP(id, sopRequestDto);
        return ResponseEntity.ok(updatedSOP);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSOP(@PathVariable String id) {
        sopService.deleteSOP(id);
        return ResponseEntity.noContent().build();
    }
}
