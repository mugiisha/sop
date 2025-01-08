package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.dto.SOPDto;
import com.sop_workflow_service.sop_workflow_service.dto.ReviewSOPDto;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.service.SOPService;
import com.sop_workflow_service.sop_workflow_service.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/sops")
public class SOPController {

    private final SOPService sopService;

    // Create SOP
    @PostMapping
    public Response<SOP> createSOP(HttpServletRequest request,@Valid @RequestBody SOPDto createSOPDto) {
        String departmentId = request.getHeader("X-Department-Id");
        SOP sop = sopService.createSOP(createSOPDto, UUID.fromString(departmentId));

        return new Response<>(true,"SOP created successfully", sop);
    }

    // Get SOP by ID
    @GetMapping("/{id}")
    public Response<SOP> getSOP(@PathVariable String id) {
        SOP sop = sopService.getSOP(id);
        return new Response<>(true, "SOP retrieved successfully", sop);
    }

    // Get  SOPs
    @GetMapping
    public Response<List<SOP>> getSOPs(HttpServletRequest request) {
        String departmentId = request.getHeader("X-Department-Id");
        List<SOP> sops = sopService.getSops(UUID.fromString(departmentId));

        return new Response<>(true, "SOPs retrieved successfully", sops);
    }

    // Get all SOPs by admin
    @GetMapping("/all")
    public Response<List<SOP>> getAllSOPs() {
        List<SOP> sops = sopService.getAllSops();
        return new Response<>(true, "SOPs retrieved successfully", sops);
    }

    // review SOP
    @PutMapping("/{id}/review")
    public Response<SOP> reviewSOP(HttpServletRequest request, @PathVariable String id, @Valid @RequestBody ReviewSOPDto reviewSOPDto) {
        String userId = request.getHeader("X-User-Id");
        SOP sop = sopService.reviewSOP(id, UUID.fromString(userId), reviewSOPDto.getComment(), reviewSOPDto.getApprovalStatus());

        return new Response<>(true, "SOP review submitted successfully", sop);
    }

    // approve SOP
    @PutMapping("/{id}/approve")
    public Response<SOP> approveSOP(HttpServletRequest request, @PathVariable String id, @Valid @RequestBody ReviewSOPDto reviewSOPDto) {
        String userId = request.getHeader("X-User-Id");
        SOP sop = sopService.approveSOP(id, UUID.fromString(userId),reviewSOPDto.getComment(), reviewSOPDto.getApprovalStatus());

        return new Response<>(true, "SOP approved successfully", sop);
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteSOP(@PathVariable String id) {
        sopService.deleteSOP(id);
        return new Response<>(true, "SOP deleted successfully", null);
    }


}
