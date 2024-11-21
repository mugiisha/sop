package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.model.Approver;
import com.sop_workflow_service.sop_workflow_service.repository.ApproverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApproverService {

    @Autowired
    private ApproverRepository approverRepository;

    public Approver createApprover(Approver approver) {
        return approverRepository.save(approver);
    }

    public List<Approver> getAllApprovers() {
        return approverRepository.findAll();
    }

    public Approver getApproverById(String id) {
        return approverRepository.findById(id).orElseThrow(() -> new RuntimeException("Approver not found with ID: " + id));
    }

    public Approver updateApprover(String id, Approver updatedApprover) {
        Approver existingApprover = approverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approver not found with ID: " + id));

        existingApprover.setName(updatedApprover.getName());
        existingApprover.setEmail(updatedApprover.getEmail());

        return approverRepository.save(existingApprover);
    }

    public void deleteApprover(String id) {
        if (!approverRepository.existsById(id)) {
            throw new RuntimeException("Approver not found with ID: " + id);
        }
        approverRepository.deleteById(id);
    }
}
