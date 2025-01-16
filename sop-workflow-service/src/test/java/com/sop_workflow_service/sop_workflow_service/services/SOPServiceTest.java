package com.sop_workflow_service.sop_workflow_service.services;

import com.sop_workflow_service.sop_workflow_service.dto.SOPDto;
import com.sop_workflow_service.sop_workflow_service.dto.UpdateStageDto;
import com.sop_workflow_service.sop_workflow_service.enums.ApprovalStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Roles;
import com.sop_workflow_service.sop_workflow_service.enums.SOPStatus;
import com.sop_workflow_service.sop_workflow_service.enums.Visibility;
import com.sop_workflow_service.sop_workflow_service.model.Category;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.model.WorkflowStage;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import com.sop_workflow_service.sop_workflow_service.service.CategoryService;
import com.sop_workflow_service.sop_workflow_service.service.SOPService;
import com.sop_workflow_service.sop_workflow_service.service.WorkflowStageService;
import com.sop_workflow_service.sop_workflow_service.utils.exception.BadRequestException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SOPServiceTest {

    @Mock
    private SOPRepository sopRepository;

    @Mock
    private WorkflowStageService workflowStageService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private KafkaTemplate<String, SOPDto> kafkaTemplate;

    @InjectMocks
    private SOPService sopService;

    private SOPDto sopDto;
    private SOP sop;
    private Category category;
    private UUID departmentId;
    private UUID initiatedBy;
    private UUID authorId;
    private UUID approverId;
    private List<UUID> reviewerIds;
    private List<WorkflowStage> workflowStages;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        initiatedBy = UUID.randomUUID();
        authorId = UUID.randomUUID();
        approverId = UUID.randomUUID();
        reviewerIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

        // Setup SOPDto
        sopDto = new SOPDto();
        sopDto.setTitle("Test SOP");
        sopDto.setCategory("Test Category");
        sopDto.setVisibility("PUBLIC");
        sopDto.setStatus(SOPStatus.DRAFTED);
        sopDto.setAuthorId(authorId);
        sopDto.setApproverId(approverId);
        sopDto.setReviewers(reviewerIds);

        // Setup Category
        category = new Category();
        category.setName("Test Category");

        // Setup SOP
        sop = new SOP();
        sop.setId(UUID.randomUUID().toString());
        sop.setTitle("Test SOP");
        sop.setCategory(category);
        sop.setVisibility(Visibility.PUBLIC);
        sop.setStatus(SOPStatus.DRAFTED);
        sop.setDepartmentId(departmentId);
        sop.setInitiatedBy(initiatedBy);
        sop.setCreatedAt(new Date());
        sop.setUpdatedAt(new Date());

        // Setup WorkflowStages
        workflowStages = new ArrayList<>();
        workflowStages.add(createWorkflowStage(sop.getId(), Roles.AUTHOR, authorId));
        reviewerIds.forEach(reviewerId ->
                workflowStages.add(createWorkflowStage(sop.getId(), Roles.REVIEWER, reviewerId))
        );
        workflowStages.add(createWorkflowStage(sop.getId(), Roles.APPROVER, approverId));
        sop.setWorkflowStages(workflowStages);
    }

    @Test
    void createSOP_Success() {
        // Arrange
        when(categoryService.getCategoryByName(any())).thenReturn(category);
        when(sopRepository.save(any(SOP.class))).thenReturn(sop);
        when(workflowStageService.saveStages(any())).thenReturn(workflowStages);

        // Act
        SOP result = sopService.createSOP(sopDto, departmentId, initiatedBy);

        // Assert
        assertNotNull(result);
        assertEquals(sop.getTitle(), result.getTitle());
        assertEquals(sop.getCategory().getName(), result.getCategory().getName());
        verify(kafkaTemplate).send(eq("sop-created"), any(SOPDto.class));
    }

    @Test
    void createSOP_CategoryNotFound() {
        // Arrange
        when(categoryService.getCategoryByName(any())).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                sopService.createSOP(sopDto, departmentId, initiatedBy)
        );
    }

    @Test
    void getSOP_Success() {
        // Arrange
        when(sopRepository.findById(sop.getId())).thenReturn(Optional.of(sop));

        // Act
        SOP result = sopService.getSOP(sop.getId());

        // Assert
        assertNotNull(result);
        assertEquals(sop.getId(), result.getId());
    }

    @Test
    void getSOP_NotFound() {
        // Arrange
        when(sopRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                sopService.getSOP(UUID.randomUUID().toString())
        );
    }

    @Test
    void reviewSOP_Success() {
        // Arrange
        sop.setStatus(SOPStatus.UNDER_REVIEWAL);
        when(sopRepository.findById(sop.getId())).thenReturn(Optional.of(sop));
        when(workflowStageService.getStagesBySopId(sop.getId())).thenReturn(workflowStages);

        // Act
        SOP result = sopService.reviewSOP(sop.getId(), reviewerIds.get(0), "Approved", ApprovalStatus.APPROVED);

        // Assert
        assertNotNull(result);
        verify(workflowStageService).updateStage(eq(reviewerIds.get(0)), eq(sop.getId()), any(UpdateStageDto.class));
    }

    @Test
    void reviewSOP_NotUnderReviewal() {
        // Arrange
        sop.setStatus(SOPStatus.DRAFTED);
        when(sopRepository.findById(sop.getId())).thenReturn(Optional.of(sop));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                sopService.reviewSOP(sop.getId(), reviewerIds.get(0), "Approved", ApprovalStatus.APPROVED)
        );
    }

    @Test
    void approveSOP_Success() {
        // Arrange
        sop.setStatus(SOPStatus.UNDER_REVIEWAL);
        // Set all reviewer stages to APPROVED
        workflowStages.stream()
                .filter(stage -> stage.getRoleRequired() == Roles.REVIEWER)
                .forEach(stage -> stage.setApprovalStatus(ApprovalStatus.APPROVED));

        when(sopRepository.findById(sop.getId())).thenReturn(Optional.of(sop));
        when(workflowStageService.getStagesBySopId(sop.getId())).thenReturn(workflowStages);

        // Act
        SOP result = sopService.approveSOP(sop.getId(), approverId, "Approved", ApprovalStatus.APPROVED);

        // Assert
        assertNotNull(result);
        verify(workflowStageService).updateStage(eq(approverId), eq(sop.getId()), any(UpdateStageDto.class));
        verify(kafkaTemplate).send(eq("sop-approved"), any(SOPDto.class));
    }

    @Test
    void approveSOP_NotAllReviewersApproved() {
        // Arrange
        sop.setStatus(SOPStatus.UNDER_REVIEWAL);
        when(sopRepository.findById(sop.getId())).thenReturn(Optional.of(sop));
        when(workflowStageService.getStagesBySopId(sop.getId())).thenReturn(workflowStages);

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                sopService.approveSOP(sop.getId(), approverId, "Approved", ApprovalStatus.APPROVED)
        );
    }

    private WorkflowStage createWorkflowStage(String sopId, Roles role, UUID userId) {
        WorkflowStage stage = new WorkflowStage();
        stage.setSopId(sopId);
        stage.setRoleRequired(role);
        stage.setUserId(userId);
        stage.setApprovalStatus(ApprovalStatus.PENDING);
        return stage;
    }
}