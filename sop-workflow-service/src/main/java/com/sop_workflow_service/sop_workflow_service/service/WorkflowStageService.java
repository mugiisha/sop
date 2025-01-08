package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.dto.UpdateStageDto;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Roles;
import com.sop_workflow_service.sop_workflow_service.model.Comment;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.WorkflowStageRepository;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkflowStageService {

    private final WorkflowStageRepository workflowStageRepository;
    private final CommentService commentService;

    public WorkflowStageService(WorkflowStageRepository workflowStageRepository, CommentService commentService) {
        this.workflowStageRepository = workflowStageRepository;
        this.commentService = commentService;
    }

    @Cacheable(value = "workflowStagesList", key = "#sopId")
    public List<WorkflowStage> getStagesBySopId(String sopId) {
        return workflowStageRepository.findBySopId(sopId);
    }

    @Cacheable(value = "workflowStage", key = "{#userId, #sopId}")
    public WorkflowStage getStageBySopIdAndUserId( String sopId,UUID userId) {
        return workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId)
                .orElseThrow(() -> new NotFoundException("User not assigned on sop"));
    }

    @Caching(evict = {
            @CacheEvict(value = "workflowStage", allEntries = true),
            @CacheEvict(value = "workflowStagesList", allEntries = true)
    })
    public WorkflowStage updateStage(UUID userId, String sopId,UpdateStageDto updateStageDto) {
        WorkflowStage stage = workflowStageRepository.findFirstBySopIdAndUserId(sopId, userId)
                .orElseThrow(() -> new NotFoundException("User not assigned on sop"));

        // A comment is required for Rejected status
        if (updateStageDto.getApprovalStatus() == ApprovalStatus.REVISION && updateStageDto.getComment() == null) {
            throw new BadRequestException("Comment is required for Rejecting an SOP");
        }

        if(updateStageDto.getComment() != null) {
            Comment newComment = commentService.saveComment(userId, stage.getId(), updateStageDto.getComment());

            if (stage.getComments() == null) {
                stage.setComments(new ArrayList<>());
            }

            stage.getComments().add(newComment);
        }

        stage.setApprovalStatus(updateStageDto.getApprovalStatus());
        return workflowStageRepository.save(stage);
    }

    public boolean isSopApproved(String sopId) {
        List<WorkflowStage> stages = workflowStageRepository.findBySopId(sopId);
        return stages.stream().allMatch(stage -> stage.getApprovalStatus() == ApprovalStatus.APPROVED);
    }

    @Caching(evict = {
            @CacheEvict(value = "workflowStage", allEntries = true),
            @CacheEvict(value = "workflowStagesList", allEntries = true)
    })
    public List<WorkflowStage> saveStages(List<WorkflowStage> stages) {
        return workflowStageRepository.saveAll(stages);
    }

    @Caching(evict = {
            @CacheEvict(value = "workflowStage", allEntries = true),
            @CacheEvict(value = "workflowStagesList", allEntries = true)
    })
    public WorkflowStage saveStage(WorkflowStage stage) {
        return workflowStageRepository.save(stage);
    }
}
