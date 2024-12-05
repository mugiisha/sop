//package com.sop_workflow_service.sop_workflow_service.controller;
//import com.sop_workflow_service.sop_workflow_service.model.SOP;
//import com.sop_workflow_service.sop_workflow_service.service.SOPService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/sops")
//public class SOPController {
//    @Autowired
//    private SOPService sopService;
//
//    // Create SOP
//    @PostMapping
//    public SOP createSOP(@RequestBody SOP sop) {
//        return sopService.createSOP(sop);
//    }
//
//    // Get SOP by ID
//    @GetMapping("/{id}")
//    public SOP getSOP(@PathVariable String id) {
//        return sopService.getSOP(id);
//    }
//
//    // Get All SOPs
//    @GetMapping
//    public List<SOP> getAllSOPs() {
//        return sopService.getAllSOPs();
//    }
//
//    // Update SOP
//    @PutMapping("/{id}")
//    public SOP updateSOP(@PathVariable String id, @RequestBody SOP sop) {
//        sop.setSopId(id);
//        return sopService.updateSOP(sop);
//    }
//
//    // Delete SOP
//    @DeleteMapping("/{id}")
//    public void deleteSOP(@PathVariable String id) {
//        sopService.deleteSOP(id);
//    }
//}
