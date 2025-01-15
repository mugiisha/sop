package com.version_control_service.version_control_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.version_control_service.version_control_service.dto.PublishedSopDto;
import com.version_control_service.version_control_service.dto.SopVersionDto;
import com.version_control_service.version_control_service.enums.Visibility;
import com.version_control_service.version_control_service.model.SOP;
import com.version_control_service.version_control_service.model.Version;
import com.version_control_service.version_control_service.model.VersionContent;
import com.version_control_service.version_control_service.repository.SopRepository;
import com.version_control_service.version_control_service.repository.VersionContentRepository;
import com.version_control_service.version_control_service.repository.VersionRepository;
import com.version_control_service.version_control_service.service.SopVersionService;
import com.version_control_service.version_control_service.utils.DtoConverter;
import com.version_control_service.version_control_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SopVersionServiceTest {

    @Mock
    private SopRepository sopRepository;
    @Mock
    private VersionContentRepository versionContentRepository;
    @Mock
    private VersionRepository versionRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private SopVersionService sopVersionService;

    private UUID testDepartmentId;
    private UUID testAuthorId;
    private UUID testApproverId;
    private List<UUID> testReviewerIds;

    @BeforeEach
    void setUp() {
        sopVersionService = new SopVersionService(
                sopRepository,
                versionContentRepository,
                versionRepository,
                kafkaTemplate
        );

        // Initialize test UUIDs
        testDepartmentId = UUID.randomUUID();
        testAuthorId = UUID.randomUUID();
        testApproverId = UUID.randomUUID();
        testReviewerIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    void updateSopVersion_WhenNewSop_ShouldCreateInitialVersion() throws JsonProcessingException {
        // Arrange
        String sopId = "test-sop-1";
        String jsonData = "{\"id\":\"test-sop-1\"}"; // Simplified JSON

        when(DtoConverter.publishedSopDtoFromJson(jsonData)).thenReturn(createSamplePublishedSopDto(sopId));
        when(sopRepository.findById(sopId)).thenReturn(Optional.empty());
        when(versionContentRepository.save(any())).thenReturn(createSampleVersionContent());
        when(versionRepository.save(any())).thenReturn(createSampleVersion(sopId, 1.0f));

        // Act
        sopVersionService.updateSopVersion(jsonData);

        // Assert
        verify(sopRepository).save(any(SOP.class));
        verify(versionContentRepository).save(any(VersionContent.class));
        verify(versionRepository).save(any(Version.class));
    }

    @Test
    void updateSopVersion_WhenExistingSop_ShouldCreateNewVersion() throws JsonProcessingException {
        // Arrange
        String sopId = "test-sop-1";
        String jsonData = "{\"id\":\"test-sop-1\"}";

        SOP existingSop = createSampleSOP(sopId);
        Version currentVersion = createSampleVersion(sopId, 1.0f);
        currentVersion.setCurrentVersion(true);

        when(DtoConverter.publishedSopDtoFromJson(jsonData)).thenReturn(createSamplePublishedSopDto(sopId));
        when(sopRepository.findById(sopId)).thenReturn(Optional.of(existingSop));
        when(versionRepository.findFirstBySopIdAndCurrentVersion(sopId, true))
                .thenReturn(currentVersion);
        when(versionRepository.findTopBySopIdOrderByVersionNumberDesc(sopId))
                .thenReturn(currentVersion);

        // Act
        sopVersionService.updateSopVersion(jsonData);

        // Assert
        verify(versionRepository).save(currentVersion);
        assertFalse(currentVersion.isCurrentVersion());
    }

    @Test
    void revertSopVersion_WhenValidVersion_ShouldRevertSuccessfully() {
        // Arrange
        String sopId = "test-sop-1";
        float oldVersion = 1.0f;
        Version currentVersion = createSampleVersion(sopId, 2.0f);
        Version targetVersion = createSampleVersion(sopId, oldVersion);
        currentVersion.setCurrentVersion(true);

        when(versionRepository.findFirstBySopIdAndCurrentVersion(sopId, true))
                .thenReturn(currentVersion);
        when(versionRepository.findFirstBySopIdAndVersionNumber(sopId, oldVersion))
                .thenReturn(targetVersion);

        // Act
        Version result = sopVersionService.revertSopVersion(sopId, oldVersion);

        // Assert
        assertFalse(currentVersion.isCurrentVersion());
        assertTrue(result.isCurrentVersion());
        verify(kafkaTemplate).send(eq("sop-version-reverted"), any(PublishedSopDto.class));
    }

    @Test
    void getSopVersions_ShouldReturnListOfVersions() {
        // Arrange
        String sopId = "test-sop-1";
        List<Version> versions = Arrays.asList(
                createSampleVersion(sopId, 1.0f),
                createSampleVersion(sopId, 2.0f)
        );

        when(versionRepository.findAllBySopId(sopId)).thenReturn(versions);

        // Act
        List<SopVersionDto> result = sopVersionService.getSopVersions(sopId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1.0f, result.get(0).getVersionNumber());
        assertEquals(2.0f, result.get(1).getVersionNumber());
    }

    @Test
    void compareSopVersions_ShouldReturnRequestedVersions() {
        // Arrange
        String sopId = "test-sop-1";
        float version1 = 1.0f;
        float version2 = 2.0f;
        List<Version> versions = Arrays.asList(
                createSampleVersion(sopId, version1),
                createSampleVersion(sopId, version2)
        );

        when(versionRepository.findAllBySopIdAndVersionNumberIn(
                eq(sopId),
                anyList()
        )).thenReturn(versions);

        // Act
        List<Version> result = sopVersionService.compareSopVersions(sopId, version1, version2);

        // Assert
        assertEquals(2, result.size());
        assertEquals(version1, result.get(0).getVersionNumber());
        assertEquals(version2, result.get(1).getVersionNumber());
    }

    // Helper methods to create test data
    private PublishedSopDto createSamplePublishedSopDto(String id) {
        return PublishedSopDto.builder()
                .id(id)
                .title("Test SOP")
                .description("Test Description")
                .body("Test Body")
                .category("Test Category")
                .departmentId(testDepartmentId)
                .coverUrl("http://example.com/cover")
                .visibility(Visibility.PUBLIC)
                .documentUrls(Arrays.asList("http://example.com/doc1"))
                .author(testAuthorId)
                .reviewers(testReviewerIds)
                .approver(testApproverId)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    private VersionContent createSampleVersionContent() {
        return VersionContent.builder()
                .title("Test SOP")
                .description("Test Description")
                .body("Test Body")
                .category("Test Category")
                .departmentId(testDepartmentId)
                .coverUrl("http://example.com/cover")
                .visibility(Visibility.PUBLIC)
                .documentUrls(Arrays.asList("http://example.com/doc1"))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    private Version createSampleVersion(String sopId, float versionNumber) {
        return Version.builder()
                .sopId(sopId)
                .versionNumber(versionNumber)
                .currentVersion(false)
                .content(createSampleVersionContent())
                .createdAt(new Date())
                .build();
    }

    private SOP createSampleSOP(String id) {
        return SOP.builder()
                .id(id)
                .versions(Collections.singletonList(createSampleVersion(id, 1.0f)))
                .build();
    }
}