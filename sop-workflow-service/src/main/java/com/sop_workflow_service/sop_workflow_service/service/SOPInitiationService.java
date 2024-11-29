package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.dto.SOPInitiationDto;
import com.sop_workflow_service.sop_workflow_service.model.SOPInitiation;
import com.sop_workflow_service.sop_workflow_service.repository.SOPInitiationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SOPInitiationService {

    @Autowired
    private SOPInitiationRepository sopInitiationRepository;

    // Create SOPInitiation
    public SOPInitiation createSOP(SOPInitiationDto sopInitiationDto) {
        SOPInitiation sopInitiation = new SOPInitiation();
        sopInitiation.setTitle(sopInitiationDto.getTitle());
        sopInitiation.setDescription(sopInitiationDto.getDescription());
        sopInitiation.setVisibility(sopInitiationDto.getVisibility());
        sopInitiation.setDepartmentId(sopInitiationDto.getDepartmentId());
        sopInitiation.setStatus("INITIATED");
        sopInitiation.setCreatedAt(LocalDateTime.now());
        sopInitiation.setUpdatedAt(LocalDateTime.now());
        return sopInitiationRepository.save(sopInitiation);
    }

    // Retrieve all SOPInitiations
    public List<SOPInitiation> getAllSOPs() {
        return sopInitiationRepository.findAll();
    }

    // Retrieve all initiated SOPInitiations
    public List<SOPInitiation> getAllInitiatedSOPs() {
        return sopInitiationRepository.findByStatus("INITIATED");
    }

    // Retrieve SOPInitiation by ID
    public SOPInitiation getSOPById(String id) {
        return sopInitiationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SOP not found with ID: " + id));
    }

    // Update SOPInitiation
    public SOPInitiation updateSOP(String id, SOPInitiationDto sopInitiationDto) {
        SOPInitiation sopInitiation = getSOPById(id);
        sopInitiation.setTitle(sopInitiationDto.getTitle());
        sopInitiation.setDescription(sopInitiationDto.getDescription());
        sopInitiation.setVisibility(sopInitiationDto.getVisibility());
        sopInitiation.setDepartmentId(sopInitiationDto.getDepartmentId());
        sopInitiation.setUpdatedAt(LocalDateTime.now());
        return sopInitiationRepository.save(sopInitiation);
    }

    // Delete SOPInitiation
    public void deleteSOP(String id) {
        sopInitiationRepository.deleteById(id);
    }
}
