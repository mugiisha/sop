package compliance_reporting_service.compliance_reporting_service;

import compliance_reporting_service.compliance_reporting_service.service.VersionClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sopVersionService.GetSopVersionsRequest;
import sopVersionService.GetSopVersionsResponse;
import sopVersionService.SopVersion;
import sopVersionService.VersionServiceGrpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VersionClientServiceTest {

    @Mock
    private VersionServiceGrpc.VersionServiceBlockingStub versionServiceBlockingStub;

    @InjectMocks
    private VersionClientService versionClientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSopVersionsSuccess() {
        // Arrange
        String sopId = "sop-123";

        SopVersion version1 = SopVersion.newBuilder()
                .setVersionNumber(1.0f)
                .setCurrentVersion(false)
                .build();
        SopVersion version2 = SopVersion.newBuilder()
                .setVersionNumber(2.0f)
                .setCurrentVersion(true)
                .build();

        GetSopVersionsResponse mockResponse = GetSopVersionsResponse.newBuilder()
                .setSuccess(true)
                .addVersions(version1)
                .addVersions(version2)
                .build();

        when(versionServiceBlockingStub.getSopVersions(any(GetSopVersionsRequest.class)))
                .thenReturn(mockResponse);

        // Act
        GetSopVersionsResponse response = versionClientService.GetSopVersions(sopId);

        // Assert
        assertEquals(true, response.getSuccess());
        assertEquals(2, response.getVersionsCount());
        assertEquals(2.0f, response.getVersions(1).getVersionNumber());
        assertEquals(true, response.getVersions(1).getCurrentVersion());

        // Verify that the gRPC stub was called once
        verify(versionServiceBlockingStub, times(1)).getSopVersions(any(GetSopVersionsRequest.class));
    }

    @Test
    void testGetSopVersionsFailure() {
        // Arrange
        String sopId = "sop-404";
        GetSopVersionsResponse mockResponse = GetSopVersionsResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage("SOP not found")
                .build();

        when(versionServiceBlockingStub.getSopVersions(any(GetSopVersionsRequest.class)))
                .thenReturn(mockResponse);

        // Act
        GetSopVersionsResponse response = versionClientService.GetSopVersions(sopId);

        // Assert
        assertEquals(false, response.getSuccess());
        assertEquals("SOP not found", response.getErrorMessage());

        // Verify that the gRPC stub was called once
        verify(versionServiceBlockingStub, times(1)).getSopVersions(any(GetSopVersionsRequest.class));
    }
}
