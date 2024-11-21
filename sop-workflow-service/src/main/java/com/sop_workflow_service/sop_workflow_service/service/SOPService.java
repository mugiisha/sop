package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.dto.SOPRequestDto;
import com.sop_workflow_service.sop_workflow_service.model.Approver;
import com.sop_workflow_service.sop_workflow_service.model.Author;
import com.sop_workflow_service.sop_workflow_service.model.Reviewer;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.repository.ApproverRepository;
import com.sop_workflow_service.sop_workflow_service.repository.AuthorRepository;
import com.sop_workflow_service.sop_workflow_service.repository.ReviewerRepository;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SOPService {

    @Autowired
    private SOPRepository sopRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ReviewerRepository reviewerRepository;

    @Autowired
    private ApproverRepository approverRepository;

    public SOP createSOP(SOPRequestDto sopRequestDto) {
        SOP sop = new SOP();
        sop.setTitle(sopRequestDto.getTitle());
        sop.setDescription(sopRequestDto.getDescription());
        sop.setVersion(sopRequestDto.getVersion());
        sop.setStatus(sopRequestDto.getStatus());

        List<Author> authors = authorRepository.findAllById(sopRequestDto.getAuthorIds());
        sop.setAuthor(authors);

        List<Reviewer> reviewers = reviewerRepository.findAllById(sopRequestDto.getReviewerIds());
        sop.setReviewers(reviewers);

        List<Approver> approvers = approverRepository.findAllById(sopRequestDto.getApproverIds());
        sop.setApprovers(approvers);

        return sopRepository.save(sop);
    }

    public List<SOP> getAllSOPs() {
        return sopRepository.findAll();
    }

    public SOP getSOPById(String id) {
        return sopRepository.findById(id).orElseThrow(() -> new RuntimeException("SOP not found with ID: " + id));
    }

    public SOP updateSOP(String id, SOPRequestDto sopRequestDto) {
        SOP sop = getSOPById(id);

        sop.setTitle(sopRequestDto.getTitle());
        sop.setDescription(sopRequestDto.getDescription());
        sop.setVersion(sopRequestDto.getVersion());
        sop.setStatus(sopRequestDto.getStatus());

        List<Author> authors = authorRepository.findAllById(sopRequestDto.getAuthorIds());
        sop.setAuthor(authors);

        List<Reviewer> reviewers = reviewerRepository.findAllById(sopRequestDto.getReviewerIds());
        sop.setReviewers(reviewers);

        List<Approver> approvers = approverRepository.findAllById(sopRequestDto.getApproverIds());
        sop.setApprovers(approvers);

        return sopRepository.save(sop);
    }

    public void deleteSOP(String id) {
        sopRepository.deleteById(id);
    }
}
