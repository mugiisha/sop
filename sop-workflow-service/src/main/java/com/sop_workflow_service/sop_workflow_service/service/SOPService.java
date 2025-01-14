package com.sop_workflow_service.sop_workflow_service.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sop_workflow_service.sop_workflow_service.dto.PublishedSopDto;
import com.sop_workflow_service.sop_workflow_service.dto.SOPDto;
import com.sop_workflow_service.sop_workflow_service.dto.SopByStatusDto;
import com.sop_workflow_service.sop_workflow_service.dto.UpdateStageDto;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Roles;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Visibility;
import com.sop_workflow_service.sop_workflow_service.model.Category;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import com.sop_workflow_service.sop_workflow_service.utils.DtoConverter;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SOPService {

    private final SOPRepository sopRepository;
    private final WorkflowStageService workflowStageService;
    private final CategoryService categoryService;
    private final KafkaTemplate<String, SOPDto> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "sops", allEntries = true)
    public SOP createSOP(SOPDto createSOPDto, UUID departmentId, UUID initiatedBy) {

        log.info("Creating SOP: {}", createSOPDto);
        //get provided Category
        Category category = categoryService.getCategoryByName(createSOPDto.getCategory());

        if(category == null){
            throw new NotFoundException("Category not found");
        }

        SOP sop = new SOP();
        sop.setTitle(createSOPDto.getTitle());
        sop.setInitiatedBy(initiatedBy);
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
        workflowStageService.saveStages(stages);

        // Set workflow stages to SOP
        createdSop.setWorkflowStages(stages);

        SOP createdSOP= sopRepository.save(createdSop);
        createSOPDto.setId(createdSOP.getId());
        createSOPDto.setDepartmentId(departmentId);
        createSOPDto.setInitiatedBy(initiatedBy);

        //send SOPDto to notify services accordingly
        createSOPDto.setCreatedAt(createdSOP.getCreatedAt());
        createSOPDto.setUpdatedAt(createdSOP.getUpdatedAt());
        kafkaTemplate.send("sop-created", createSOPDto);

        return createdSOP;
    }

    // Get SOP by ID with updated Workflow Stages
    @Cacheable(value = "sop", key = "#id")
    public SOP getSOP(String id) {
        log.info("Getting SOP with id: {}", id);

        return sopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SOP not found"));
    }

    // public get sops getting
    @Cacheable(value = "sops", key = "#departmentId")
    public List<SOP>  getSops(UUID departmentId){
        log.info("Getting SOPs with departmentId: {}", departmentId);

        return sopRepository.findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(departmentId, Visibility.PUBLIC);
    }

    // Get All SOPs
    public List<SOP> getAllSops() {
        log.info("Fetching all SOPs");
        return sopRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get SOPs by departmentId and status
    @Cacheable(value = "sopsByStatus", key = "#departmentId")
    public List<SopByStatusDto> getSopsByDepartmentId(UUID departmentId) {
        log.info("Fetching SOPs by departmentId: {}", departmentId);
        return sopRepository.findAllByDepartmentId(departmentId)
                .stream()
                .map(this::mapSopToSopByStatusDto)
                .collect(Collectors.toList());
    }

    // Get SOPs by status
    @Cacheable(value = "allSopsByStatus")
    public List<SopByStatusDto> getSops() {
        log.info("Fetching SOPs");
        return sopRepository.findAll()
                .stream()
                .map(this::mapSopToSopByStatusDto)
                .collect(Collectors.toList());
    }

    // Delete SOP
    @Caching(evict = {
            @CacheEvict(value = "sop", allEntries = true),
            @CacheEvict(value = "sops", allEntries = true),
            @CacheEvict(value = "sopsByStatus", allEntries = true),
            @CacheEvict(value = "allSopsByStatus", allEntries = true)
    })
    public void deleteSOP(String id) {
        SOP sop = sopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        sopRepository.delete(sop);
        //send deleted SOP event to delete it's content too
        SOPDto sopDto = mapSOPToSOPDto(sop);
        kafkaTemplate.send("sop-deleted", sopDto);
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "sop", allEntries = true),
            @CacheEvict(value = "sops", allEntries = true)
    })
    public SOP reviewSOP(String sopId, UUID userId, String comment, ApprovalStatus approvalStatus) {

        SOP sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        if(sop.getStatus() != SOPStatus.UNDER_REVIEWAL ){
            throw new BadRequestException("SOP is not under reviewal stage");
        }

        UpdateStageDto updateStageDto = UpdateStageDto.builder()
                .approvalStatus(approvalStatus)
                .comment(comment)
                .build();

        workflowStageService.updateStage(userId, sopId, updateStageDto);

        SOPDto sopDto = mapSOPToSOPDto(sop);

        if(approvalStatus == ApprovalStatus.REVISION){
            kafkaTemplate.send("sop-revision", sopDto);
        }

        List<WorkflowStage> stages = workflowStageService.getStagesBySopId(sopId);

        // Check if all reviewers have reviewed to notify the approver
        boolean allReviewersReviewed = stages.stream()
                .filter(s -> s.getRoleRequired() == Roles.REVIEWER)
                .allMatch(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED);

        if(allReviewersReviewed){
            //notify approver
            kafkaTemplate.send("sop-reviewed", sopDto);
        }

        return sop;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "sop", allEntries = true),
            @CacheEvict(value = "sops", allEntries = true)
    })
    public SOP approveSOP(String sopId, UUID userId, String comment, ApprovalStatus approvalStatus) {

        SOP sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        List<WorkflowStage> stages = workflowStageService.getStagesBySopId(sopId);
        // Check if all reviewers have reviewed to allow approval
        boolean allReviewersReviewed = stages.stream()
                .filter(s -> s.getRoleRequired() == Roles.REVIEWER)
                .allMatch(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED);

        if(approvalStatus== ApprovalStatus.APPROVED && !allReviewersReviewed){
            throw new BadRequestException("Approving not allowed before all reviewers review the SOP");
        }

        UpdateStageDto updateStageDto = UpdateStageDto.builder()
                .approvalStatus(approvalStatus)
                .comment(comment)
                .build();

        workflowStageService.updateStage(userId, sopId, updateStageDto);


        SOPDto sopDto = mapSOPToSOPDto(sop);

        if(approvalStatus == ApprovalStatus.REVISION){
            kafkaTemplate.send("sop-revision", sopDto);
        }

        kafkaTemplate.send("sop-approved", sopDto);

        return sop;
    }



    @KafkaListener(topics = "sop-drafted")
    @Caching(evict = {
            @CacheEvict(value = "sop", allEntries = true),
            @CacheEvict(value = "sops", allEntries = true),
            @CacheEvict(value = "sopsByStatus", allEntries = true),
            @CacheEvict(value = "allSopsByStatus", allEntries = true)
    })
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
    @Caching(evict = {
            @CacheEvict(value = "sop", allEntries = true),
            @CacheEvict(value = "sops", allEntries = true),
            @CacheEvict(value = "sopsByStatus", allEntries = true),
            @CacheEvict(value = "allSopsByStatus", allEntries = true)
    })
    public void sopReviewalReadyListener(String data) throws JsonProcessingException {

        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
        // update the sop status to drafted
        log.info("SOP reviewal ready: {}", sopDto.getId());

        SOP sop = sopRepository.findById(sopDto.getId())
                .orElseThrow(() -> new NotFoundException("SOP not found"));

        // Set author workflow stage to approved
        WorkflowStage authorWorkflowStage = workflowStageService.getStageBySopIdAndUserId(sopDto.getId(),sopDto.getAuthorId());
        authorWorkflowStage.setApprovalStatus(ApprovalStatus.APPROVED);
        workflowStageService.saveStage(authorWorkflowStage);

        // If the sop was published, set reviewers and approver stages to pending
        if(sop.getStatus() == SOPStatus.PUBLISHED){
            List<WorkflowStage> reviewersStages = new ArrayList<>();

            for(UUID reviewerId : sopDto.getReviewers()){
                WorkflowStage reviewerStage = workflowStageService.getStageBySopIdAndUserId(sopDto.getId(),reviewerId);
                reviewerStage.setApprovalStatus(ApprovalStatus.PENDING);
                reviewersStages.add(reviewerStage);
            }

            workflowStageService.saveStages(reviewersStages);

            WorkflowStage approverWorkflowStage = workflowStageService.getStageBySopIdAndUserId(sopDto.getId(),sopDto.getApproverId());
            approverWorkflowStage.setApprovalStatus(ApprovalStatus.PENDING);
            workflowStageService.saveStage(approverWorkflowStage);
        }


        sop.setStatus(SOPStatus.UNDER_REVIEWAL);
        sopRepository.save(sop);
    }

    @KafkaListener(topics = "sop-published")
    @Caching(evict = {
            @CacheEvict(value = "sop", allEntries = true),
            @CacheEvict(value = "sops", allEntries = true),
            @CacheEvict(value = "sopsByStatus", allEntries = true),
            @CacheEvict(value = "allSopsByStatus", allEntries = true)
    })
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

public SopByStatusDto mapSopToSopByStatusDto(SOP sop) {
        return SopByStatusDto
                .builder()
                .id(sop.getId())
                .title(sop.getTitle())
                .status(sop.getStatus())
                .createdAt(sop.getCreatedAt())
                .updatedAt(sop.getUpdatedAt())
                .build();
    }
}