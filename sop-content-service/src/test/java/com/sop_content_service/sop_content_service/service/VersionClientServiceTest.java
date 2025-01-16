package com.sop_content_service.sop_content_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopVersionService.*;
import sopVersionService.VersionServiceGrpc.VersionServiceBlockingStub;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VersionClientServiceTest {

    @Mock
    private VersionServiceBlockingStub versionServiceBlockingStub;

    @InjectMocks
    private VersionClientService versionClientService;

    private static final String TEST_SOP_ID = "sop123";

    @BeforeEach
    void setUp() {
        // Method intentionally left empty as mocks are handled by annotations
    }

    @Test
    void GetSopVersions_ShouldReturnValidResponse() {
        // Arrange
        SopVersion version1 = SopVersion.newBuilder()
                .setVersionNumber(1.0f)
                .setCurrentVersion(true)
                .build();

        SopVersion version2 = SopVersion.newBuilder()
                .setVersionNumber(0.9f)
                .setCurrentVersion(false)
                .build();

        GetSopVersionsResponse expectedResponse = GetSopVersionsResponse.newBuilder()
                .setSuccess(true)
                .addAllVersions(Arrays.asList(version1, version2))
                .build();

        when(versionServiceBlockingStub.getSopVersions(any(GetSopVersionsRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        GetSopVersionsResponse actualResponse = versionClientService.GetSopVersions(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(2, actualResponse.getVersionsCount());

        List<SopVersion> versions = actualResponse.getVersionsList();

        // Check first version
        assertEquals(1.0f, versions.get(0).getVersionNumber());
        assertTrue(versions.get(0).getCurrentVersion());

        // Check second version
        assertEquals(0.9f, versions.get(1).getVersionNumber());
        assertFalse(versions.get(1).getCurrentVersion());
    }

    @Test
    void GetSopVersions_ShouldHandleEmptyVersionsList() {
        // Arrange
        GetSopVersionsResponse expectedResponse = GetSopVersionsResponse.newBuilder()
                .setSuccess(true)
                .build();

        when(versionServiceBlockingStub.getSopVersions(any(GetSopVersionsRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        GetSopVersionsResponse actualResponse = versionClientService.GetSopVersions(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(0, actualResponse.getVersionsCount());
        assertTrue(actualResponse.getVersionsList().isEmpty());
    }

    @Test
    void GetSopVersions_ShouldHandleErrorResponse() {
        // Arrange
        String errorMessage = "SOP not found";
        GetSopVersionsResponse errorResponse = GetSopVersionsResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(errorMessage)
                .build();

        when(versionServiceBlockingStub.getSopVersions(any(GetSopVersionsRequest.class)))
                .thenReturn(errorResponse);

        // Act
        GetSopVersionsResponse actualResponse = versionClientService.GetSopVersions(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.getSuccess());
        assertEquals(errorMessage, actualResponse.getErrorMessage());
        assertTrue(actualResponse.getVersionsList().isEmpty());
    }

    @Test
    void GetSopVersions_ShouldHandleSingleVersion() {
        // Arrange
        SopVersion version = SopVersion.newBuilder()
                .setVersionNumber(1.0f)
                .setCurrentVersion(true)
                .build();

        GetSopVersionsResponse expectedResponse = GetSopVersionsResponse.newBuilder()
                .setSuccess(true)
                .addVersions(version)
                .build();

        when(versionServiceBlockingStub.getSopVersions(any(GetSopVersionsRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        GetSopVersionsResponse actualResponse = versionClientService.GetSopVersions(TEST_SOP_ID);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.getSuccess());
        assertEquals(1, actualResponse.getVersionsCount());

        SopVersion returnedVersion = actualResponse.getVersions(0);
        assertEquals(1.0f, returnedVersion.getVersionNumber());
        assertTrue(returnedVersion.getCurrentVersion());
    }
}