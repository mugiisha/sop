package com.sop_workflow_service.sop_workflow_service.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sop_workflow_service.sop_workflow_service.dto.PublishedSopDto;
import com.sop_workflow_service.sop_workflow_service.dto.SOPDto;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Roles;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Visibility;
import com.sop_workflow_service.sop_workflow_service.model.Category;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.CommentRepository;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import com.sop_workflow_service.sop_workflow_service.utils.DtoConverter;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userService.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SOPService {

    private final SOPRepository sopRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final CategoryService categoryService;
    private final KafkaTemplate<String, SOPDto> kafkaTemplate;
    private final CommentRepository commentRepository;


    @Transactional
    public SOP createSOP(SOPDto createSOPDto, UUID departmentId) {

        log.info("Creating SOP: {}", createSOPDto);
        //get provided Category
        Category category = categoryService.getCategoryByName(createSOPDto.getCategory());

        if(category == null){
            throw new NotFoundException("Category not found");
        }

        SOP sop = new SOP();
        sop.setTitle(createSOPDto.getTitle());
        sop.setVisibility(Visibility.valueOf(createSOPDto.getVisibility().toUpperCase()));
        sop.setStatus(createSOPDto.getStatus());
        sop.setDepartmentId(departmentId);
        sop.setCategory(category);
        sop.setCreatedAt(new Date());
        sop.setUpdatedAt(new Date());


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

        SOP createdSOP= sopRepository.save(createdSop);
        createSOPDto.setId(createdSOP.getId());
        createSOPDto.setDepartmentId(departmentId);

        //send SOPDto to notify services accordingly
        createSOPDto.setCreatedAt(createdSOP.getCreatedAt());
        createSOPDto.setUpdatedAt(createdSOP.getUpdatedAt());
        kafkaTemplate.send("sop-created", createSOPDto);

        return createdSOP;
    }

    // Get SOP by ID with updated Workflow Stages
    public SOP getSOP(String id) {
        log.info("Getting SOP with id: {}", id);

       return sopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SOP not found"));
    }

    // public get sops getting
    public List<SOP>  getSops(UUID departmentId){
        log.info("Getting SOPs with departmentId: {}", departmentId);

        return sopRepository.findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(departmentId, Visibility.PUBLIC);
    }

    // Get All SOPs
    public List<SOP> getAllSops() {
        log.info("Fetching all SOPs");
       return sopRepository.findAllByOrderByCreatedAtDesc();
    }

    // Delete SOP
    public void deleteSOP(String id) {
        SOP sop = sopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        sopRepository.delete(sop);
        //send deleted SOP event to delete it's content too
        SOPDto sopDto = mapSOPToSOPDto(sop);
        kafkaTemplate.send("sop-deleted", sopDto);
    }

    public WorkflowStage getStageByUserIdAndSopId(UUID userId, String sopId) {
        return workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId)
                .orElseThrow(() -> new NotFoundException("Stage not found"));
    }


    @Transactional
    public SOP reviewSOP(String sopId, UUID userId, String comment, ApprovalStatus approvalStatus) {

        SOP sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        if(sop.getStatus() == SOPStatus.DRAFTED || sop.getStatus() == SOPStatus.INITIALIZED  ){
            throw new BadRequestException("SOP is not yet ready for review");
        }

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
            Comment savedComment = commentRepository.save(newComment);
            // Ensure comments list is initialized if no comment is present
            if (stage.getComments() == null) {
                stage.setComments(new ArrayList<>());
            }
            stage.getComments().add(savedComment);
        }

        workflowStageRepository.save(stage);

        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sopId);

        // Check if all reviewers have reviewed to notify the approver
        boolean allReviewersReviewed = stages.stream()
                .filter(s -> s.getRoleRequired() == Roles.REVIEWER)
                .allMatch(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED);

        if(allReviewersReviewed){
            //notify approver
            SOPDto sopDto = mapSOPToSOPDto(sop);
            kafkaTemplate.send("sop-reviewed", sopDto);
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
            Comment savedComment = commentRepository.save(newComment);
            // Ensure comments list is initialized if no comment is present
            if (stage.getComments() == null) {
                stage.setComments(new ArrayList<>());
            }

            stage.getComments().add(savedComment);
        }

        workflowStageRepository.save(stage);

        SOPDto sopDto = mapSOPToSOPDto(sop);
        kafkaTemplate.send("sop-approved", sopDto);

        return sop;
    }

    public boolean isSopApproved(String sopId) {
        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sopId);
        return stages.stream().allMatch(stage -> stage.getApprovalStatus() == ApprovalStatus.APPROVED);
    }


    @KafkaListener(topics = "sop-drafted")
    public void sopDraftedListener(String data) throws JsonProcessingException {

        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
        // update the sop status to drafted
        log.info("SOP Drafted: {}", sopDto.getId());

        SOP sop = sopRepository.findById(sopDto.getId())
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        sop.setStatus(SOPStatus.DRAFTED);
        sopRepository.save(sop);
    }

    @KafkaListener(topics = "sop-reviewal-ready")
    public void sopReviewalReadyListener(String data) throws JsonProcessingException {

        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
        // update the sop status to drafted
        log.info("SOP reviewal ready: {}", sopDto.getId());

        SOP sop = sopRepository.findById(sopDto.getId())
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        WorkflowStage authorWorkflowStage =
                workflowStageRepository.findFirstBySopIdAndUserId(sopDto.getId(),sopDto.getAuthorId())
                        .orElse(null);

        if(authorWorkflowStage != null){
            authorWorkflowStage.setApprovalStatus(ApprovalStatus.APPROVED);
            workflowStageRepository.save(authorWorkflowStage);
        }


        sop.setStatus(SOPStatus.UNDER_REVIEWAL);
        sopRepository.save(sop);
    }

    @KafkaListener(topics = "sop-published")
    public void sopPublishedListener(String data) throws JsonProcessingException {

        PublishedSopDto publishedSopDto = DtoConverter.publishedSopDtoFromJson(data);
        // update the sop status to drafted
        log.info("SOP published: {}", publishedSopDto.getId());

        SOP sop = sopRepository.findById(publishedSopDto.getId())
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        sop.setStatus(SOPStatus.PUBLISHED);
        sopRepository.save(sop);
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

    public SOPDto mapSOPToSOPDto(SOP sop) {
        SOPDto sopDto = new SOPDto();
        sopDto.setId(sop.getId());
        sopDto.setTitle(sop.getTitle());
        sopDto.setCategory(sop.getCategory().getName());
        sopDto.setDepartmentId(sop.getDepartmentId());
        sopDto.setAuthorId(sop.getWorkflowStages().stream()
                .filter(stage -> stage.getRoleRequired() == Roles.AUTHOR)
                .map(WorkflowStage::getUserId)
                .findFirst().get()
        );
        sopDto.setReviewers(sop.getWorkflowStages().stream()
                .filter(stage -> stage.getRoleRequired() == Roles.REVIEWER)
                .map(WorkflowStage::getUserId)
                .collect(Collectors.toList()));
        sopDto.setApproverId(sop.getWorkflowStages().stream()
                .filter(stage -> stage.getRoleRequired() == Roles.APPROVER)
                .map(WorkflowStage::getUserId)
                .findFirst().get()
        );
        return sopDto;
    }
}
