package com.sop_content_service.sop_content_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sop_content_service.sop_content_service.dto.*;
import com.sop_content_service.sop_content_service.enums.ApprovalStatus;
import com.sop_content_service.sop_content_service.enums.SOPStatus;
import com.sop_content_service.sop_content_service.enums.Visibility;
import com.sop_content_service.sop_content_service.exception.BadInputRequest;
import com.sop_content_service.sop_content_service.exception.BadRequestException;
import com.sop_content_service.sop_content_service.exception.SopNotFoundException;
import com.sop_content_service.sop_content_service.model.Sop;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sopVersionService.GetSopVersionsResponse;
import sopWorkflowService.GetWorkflowStageInfoResponse;
import sopWorkflowService.IsSOPApprovedResponse;
import userService.getDepartmentNameResponse;
import userService.getUserInfoResponse;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SopServiceTest {

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private SopRepository sopRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WorkflowClientService workflowClientService;

    @Mock
    private UserInfoClientService userInfoClientService;

    @Mock
    private VersionClientService versionClientService;

    private SopService sopService;

    @BeforeEach
    void setUp() {
        sopService = new SopService(s3Client, sopRepository, kafkaTemplate,
                workflowClientService, userInfoClientService, versionClientService);
    }

    @Test
    void addSopContent_Success() throws IOException {
        // Arrange
        String sopId = "test-sop-id";
        UUID authorId = UUID.randomUUID();
        MockMultipartFile document = new MockMultipartFile(
                "document", "test.pdf", "application/pdf", "test content".getBytes()
        );
        MockMultipartFile coverImage = new MockMultipartFile(
                "cover", "cover.jpg", "image/jpeg", "image content".getBytes()
        );
        SopContentDto contentDto = new SopContentDto();
        contentDto.setDescription("Test description");
        contentDto.setBody("Test body");
        contentDto.setStatus(SOPStatus.UNDER_REVIEWAL);

        Sop existingSop = new Sop();
        existingSop.setId(sopId);
        existingSop.setAuthor(authorId);

        when(sopRepository.findById(sopId)).thenReturn(Optional.of(existingSop));
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("http://test-url.com"));
        when(sopRepository.save(any(Sop.class))).thenReturn(existingSop);

        // Act
        Sop result = sopService.addSopContent(sopId, List.of(document), coverImage, contentDto, authorId);

        // Assert
        assertNotNull(result);
        verify(sopRepository).save(any(Sop.class));
        verify(kafkaTemplate).send(eq("sop-reviewal-ready"), any(SOPDto.class));
    }

    @Test
    void addSopContent_SopNotFound() {
        // Arrange
        String sopId = "non-existent-id";
        when(sopRepository.findById(sopId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SopNotFoundException.class, () ->
                sopService.addSopContent(sopId, null, null, new SopContentDto(), UUID.randomUUID())
        );
    }

    @Test
    void addSopContent_UnauthorizedUser() {
        // Arrange
        String sopId = "test-sop-id";
        UUID authorId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        Sop existingSop = new Sop();
        existingSop.setId(sopId);
        existingSop.setAuthor(authorId);

        when(sopRepository.findById(sopId)).thenReturn(Optional.of(existingSop));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                sopService.addSopContent(sopId, null, null, new SopContentDto(), differentUserId)
        );
    }

    @Test
    void getSops_Success() {
        // Arrange
        UUID departmentId = UUID.randomUUID();
        List<Sop> mockSops = Arrays.asList(
                createMockSop("sop1"),
                createMockSop("sop2")
        );

        when(sopRepository.findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(
                departmentId, Visibility.PUBLIC)).thenReturn(mockSops);
        setupMockResponses();

        // Act
        List<SOPResponseDto> result = sopService.getSops(departmentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sopRepository).findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(
                departmentId, Visibility.PUBLIC);
    }

    @Test
    void publishSop_Success() {
        // Arrange
        String sopId = "test-sop-id";
        Sop existingSop = createMockSop(sopId);

        when(sopRepository.findById(sopId)).thenReturn(Optional.of(existingSop));
        when(workflowClientService.isSOPApproved(sopId))
                .thenReturn(IsSOPApprovedResponse.newBuilder()
                        .setSuccess(true)
                        .setSOPApproved(true)
                        .build());
        when(sopRepository.save(any(Sop.class))).thenReturn(existingSop);
        setupMockResponses();

        // Act
        SOPResponseDto result = sopService.publishSop(sopId);

        // Assert
        assertNotNull(result);
        assertEquals(SOPStatus.PUBLISHED, existingSop.getStatus());
        verify(kafkaTemplate).send(eq("sop-published"), any(PublishedSopDto.class));
    }

    @Test
    void publishSop_NotApproved() {
        // Arrange
        String sopId = "test-sop-id";
        Sop existingSop = createMockSop(sopId);

        when(sopRepository.findById(sopId)).thenReturn(Optional.of(existingSop));
        when(workflowClientService.isSOPApproved(sopId))
                .thenReturn(IsSOPApprovedResponse.newBuilder()
                        .setSuccess(true)
                        .setSOPApproved(false)
                        .build());

        // Act & Assert
        assertThrows(BadInputRequest.class, () -> sopService.publishSop(sopId));
    }

    private Sop createMockSop(String id) {
        Sop sop = new Sop();
        sop.setId(id);
        sop.setTitle("Test SOP");
        sop.setDescription("Test Description");
        sop.setBody("Test Body");
        sop.setStatus(SOPStatus.UNDER_REVIEWAL);
        sop.setVisibility(Visibility.PUBLIC);
        sop.setAuthor(UUID.randomUUID());
        sop.setReviewers(List.of(UUID.randomUUID()));
        sop.setApprover(UUID.randomUUID());
        sop.setCreatedAt(new Date());
        sop.setUpdatedAt(new Date());
        return sop;
    }

    private void setupMockResponses() {
        // Mock version client response
        when(versionClientService.GetSopVersions(anyString()))
                .thenReturn(GetSopVersionsResponse.newBuilder()
                        .setSuccess(true)
                        .build());

        // Mock department name response
        when(userInfoClientService.getDepartmentName(anyString()))
                .thenReturn(getDepartmentNameResponse.newBuilder()
                        .setSuccess(true)
                        .setDepartmentName("Test Department")
                        .build());

        // Mock user info response
        when(userInfoClientService.getUserInfo(anyString()))
                .thenReturn(getUserInfoResponse.newBuilder()
                        .setSuccess(true)
                        .setName("Test User")
                        .setProfilePictureUrl("http://test-url.com/profile.jpg")
                        .build());

        // Mock workflow stage response
        when(workflowClientService.getWorkflowStage(anyString(), anyString()))
                .thenReturn(GetWorkflowStageInfoResponse.newBuilder()
                        .setSuccess(true)
                        .setStatus(ApprovalStatus.PENDING.name())
                        .build());
    }
}