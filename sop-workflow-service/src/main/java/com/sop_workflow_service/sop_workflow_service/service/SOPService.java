package com.sop_workflow_service.sop_workflow_service.service;


import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SOPService {

    private final SOPRepository sopRepository;

    public SOP createSOP(SOP sop) {
        sop.setStatus("INITIATED");
        sop.setCreatedAt(LocalDateTime.now());
        sop.setUpdatedAt(LocalDateTime.now());
        return sopRepository.save(sop);
    }

    public List<SOP> getAllSOPs() {
        return sopRepository.findAll();
    }

    public SOP getSOPById(Long id) {
        return sopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SOP not found"));
    }

    public SOP updateSOP(Long id, SOP updatedSop) {
        SOP sop = getSOPById(id);
        sop.setTitle(updatedSop.getTitle());
        sop.setVisibility(updatedSop.getVisibility());
        sop.setAuthorId(updatedSop.getAuthorId());
        sop.setReviewerIds(updatedSop.getReviewerIds());
        sop.setApproverId(updatedSop.getApproverId());
        sop.setStatus(updatedSop.getStatus());
        sop.setUpdatedAt(LocalDateTime.now());
        return sopRepository.save(sop);
    }

    public void deleteSOP(Long id) {
        sopRepository.deleteById(id);
    }
}