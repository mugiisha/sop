package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.dto.CreateSOPDto;
import com.sop_workflow_service.sop_workflow_service.dto.SOPResponseDto;
import com.sop_workflow_service.sop_workflow_service.dto.StageDto;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Roles;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Visibility;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userService.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SOPService {

    private final SOPRepository sopRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final UserInfoClientService userInfoClientService;


    @Transactional
    public SOP createSOP(CreateSOPDto createSOPDto, UUID departmentId) {

        log.info("Creating SOP: {}", createSOPDto);

        SOP sop = new SOP();
        sop.setTitle(createSOPDto.getTitle());
        sop.setCategory(createSOPDto.getCategory());
        sop.setVisibility(Visibility.valueOf(createSOPDto.getVisibility().toUpperCase()));
        sop.setStatus(SOPStatus.INITIALIZED);
        sop.setDepartmentId(departmentId);
        sop.setCreatedAt(LocalDateTime.now());
        sop.setUpdatedAt(LocalDateTime.now());

        SOP createdSop = sopRepository.save(sop);

        // Create workflow stages
        List<WorkflowStage> stages = new ArrayList<>();
        stages.add(createWorkflowStage(createdSop.getId(), Roles.AUTHOR, createSOPDto.getAuthorId()));

        for (UUID reviewerId : createSOPDto.getReviewers()) {
            stages.add(createWorkflowStage(createdSop.getId(), Roles.REVIEWER, reviewerId));
        }

        stages.add(createWorkflowStage(createdSop.getId(), Roles.APPROVER, createSOPDto.getApproverId()));

        // Save workflow stages
        workflowStageRepository.saveAll(stages);

        // Set workflow stages to SOP
        createdSop.setWorkflowStages(stages);

        return sopRepository.save(createdSop);
    }

    // Get SOP by ID with updated Workflow Stages
    public SOPResponseDto getSOP(String id) {
        log.info("Getting SOP with id: {}", id);

        SOP sop = sopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        return mapSOPToSOPResponseDto(sop);

    }

    // Get All SOPs
    public List<SOP> getDepartmentSops(UUID departmentId) {
        log.info("Querying SOPs for department: {}", departmentId);
        return sopRepository.findByDepartmentId(departmentId);
    }



    // Delete SOP
    public void deleteSOP(String id) {
        sopRepository.deleteById(id);
    }


    @Transactional
    public SOP reviewSOP(String sopId, UUID userId, String comment, ApprovalStatus approvalStatus) {

        SOP sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        Optional<WorkflowStage> optionalStage = workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId);

        if (optionalStage.isEmpty()) {
            throw new NotFoundException("User not assigned on sop");
        }

        WorkflowStage stage = optionalStage.get();

        // A comment is required for Rejected status
        if (approvalStatus == ApprovalStatus.REVISION && comment == null) {
            throw new IllegalArgumentException("Comment is required for Rejecting an SOP");
        }

        stage.setApprovalStatus(approvalStatus);
        stage.setUpdatedAt(LocalDateTime.now());

        if (comment != null) {
            Comment newComment = new Comment();
            newComment.setUserId(userId);
            newComment.setStageId(stage.getId());
            newComment.setContent(comment);
            newComment.setCreatedAt(LocalDateTime.now());
            newComment.setUpdatedAt(LocalDateTime.now());
            stage.getComments().add(newComment);
        }

        workflowStageRepository.save(stage);

        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sopId);

        // Check if all reviewers have reviewed to notify the approver
        boolean allReviewersReviewed = stages.stream()
                .filter(s -> s.getRoleRequired() == Roles.REVIEWER)
                .allMatch(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED);

        if(allReviewersReviewed){
            //notify approver
        }

        return sop;
    }

    @Transactional
    public SOP approveSOP(String sopId, UUID userId, String comment, ApprovalStatus approvalStatus) {

        SOP sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        Optional<WorkflowStage> optionalStage = workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId);

        if (optionalStage.isEmpty()) {
            throw new NotFoundException("user not assigned on sop");
        }

        WorkflowStage stage = optionalStage.get();

        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sopId);
        // Check if all reviewers have reviewed to allow approval
        boolean allReviewersReviewed = stages.stream()
                .filter(s -> s.getRoleRequired() == Roles.REVIEWER)
                .allMatch(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED);

        if(approvalStatus== ApprovalStatus.APPROVED && !allReviewersReviewed){
           throw new IllegalArgumentException("Approving not allowed before all reviewers review the SOP");
        }

        // require comment for rejected status
        if (approvalStatus == ApprovalStatus.REVISION && comment == null) {
            throw new IllegalArgumentException("Comment is required for Rejecting an SOP");
        }

        stage.setApprovalStatus(approvalStatus);
        stage.setUpdatedAt(LocalDateTime.now());

        if (comment != null) {
            Comment newComment = new Comment();
            newComment.setUserId(userId);
            newComment.setContent(comment);
            newComment.setCreatedAt(LocalDateTime.now());
            newComment.setUpdatedAt(LocalDateTime.now());
            stage.getComments().add(newComment);
        }

        workflowStageRepository.save(stage);

        return sop;
    }


    private WorkflowStage createWorkflowStage(String sopId, Roles roleRequired, UUID userId) {
        WorkflowStage stage = new WorkflowStage();
        stage.setSopId(sopId);
        stage.setRoleRequired(roleRequired);
        stage.setUserId(userId);
        stage.setApprovalStatus(ApprovalStatus.PENDING);
        stage.setCreatedAt(LocalDateTime.now());
        stage.setUpdatedAt(LocalDateTime.now());
        return stage;
    }

    private StageDto createStageDto(WorkflowStage stage) {
        if (stage == null) {
            return null;
        }
        getUserInfoResponse userInfo = userInfoClientService.getUserInfo(stage.getUserId().toString());

        if(!userInfo.getSuccess()){
            log.error("Error fetching user info: {}", userInfo.getErrorMessage());
        }

        StageDto stageDto = new StageDto();
        stageDto.setUserId(stage.getUserId());
        stageDto.setName(userInfo.getName());
        stageDto.setProfilePictureUrl(userInfo.getProfilePictureUrl());
        stageDto.setStatus(stage.getApprovalStatus().name());

        if(stage.getComments() != null){
            stageDto.setComments(stage.getComments().stream().map(Comment::getContent).collect(Collectors.toList()));
        }
        return stageDto;
    }


    public SOPResponseDto mapSOPToSOPResponseDto(SOP sop) {
        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sop.getId());

        SOPResponseDto response = new SOPResponseDto();
        response.setId(sop.getId());
        response.setTitle(sop.getTitle());
        response.setStatus(sop.getStatus());

        List<StageDto> reviewers = new ArrayList<>();


        for (WorkflowStage stage : stages) {
            StageDto stageDto = createStageDto(stage);
            if (stage.getRoleRequired() == Roles.AUTHOR) {
                response.setAuthor(stageDto);
            } else if (stage.getRoleRequired() == Roles.REVIEWER) {
                reviewers.add(stageDto);
            } else if (stage.getRoleRequired() == Roles.APPROVER) {
                response.setApprover(stageDto);
            }
        }

        response.setReviewers(reviewers);

        return response;
    }

    // Get All SOPs
    public List<SOP> getAllSops() {
        log.info("Retrieving all SOPs across all departments");
        return sopRepository.findAll();
    }

}
