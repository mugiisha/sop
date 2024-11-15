package com.sop_workflow_service.sop_workflow_service.controller;


import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.service.SOPService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sops")
@RequiredArgsConstructor
public class SOPController {

    private final SOPService sopService;

    @PostMapping
    public SOP createSOP(@RequestBody SOP sop) {
        return sopService.createSOP(sop);
    }

    @GetMapping
    public List<SOP> getAllSOPs() {
        return sopService.getAllSOPs();
    }

    @GetMapping("/{id}")
    public SOP getSOPById(@PathVariable Long id) {
        return sopService.getSOPById(id);
    }

    @PutMapping("/{id}")
    public SOP updateSOP(@PathVariable Long id, @RequestBody SOP updatedSop) {
        return sopService.updateSOP(id, updatedSop);
    }

    @DeleteMapping("/{id}")
    public void deleteSOP(@PathVariable Long id) {
        sopService.deleteSOP(id);
    }
}