package com.version_control_service.version_control_service.services;

import com.version_control_service.version_control_service.dto.SopVersionDto;
import com.version_control_service.version_control_service.service.SopVersionService;
import com.version_control_service.version_control_service.service.VersionGrpcService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sopVersionService.GetSopVersionsRequest;
import sopVersionService.GetSopVersionsResponse;
import sopVersionService.SopVersion;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VersionGrpcServiceTest {

    private VersionGrpcService versionGrpcService;

    @Mock
    private SopVersionService sopVersionService;

    @Mock
    private StreamObserver<GetSopVersionsResponse> responseObserver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        versionGrpcService = new VersionGrpcService(sopVersionService);
    }

    @Test
    void getSopVersions_Success() {
        // Arrange
        String sopId = "test-sop-id";
        GetSopVersionsRequest request = GetSopVersionsRequest.newBuilder()
                .setSopId(sopId)
                .build();

        Date now = new Date();
        List<SopVersionDto> mockVersionDtos = Arrays.asList(
                SopVersionDto.builder()
                        .versionNumber(1.0f)
                        .currentVersion(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                SopVersionDto.builder()
                        .versionNumber(2.0f)
                        .currentVersion(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        when(sopVersionService.getSopVersions(sopId)).thenReturn(mockVersionDtos);

        // Act
        versionGrpcService.getSopVersions(request, responseObserver);

        // Assert
        verify(sopVersionService).getSopVersions(sopId);

        verify(responseObserver).onNext(argThat(response -> {
            GetSopVersionsResponse resp = (GetSopVersionsResponse) response;
            return resp.getSuccess() &&
                    resp.getVersionsList().size() == 2 &&
                    resp.getVersionsList().get(0).getVersionNumber() == 1.0f &&
                    resp.getVersionsList().get(0).getCurrentVersion() &&
                    resp.getVersionsList().get(1).getVersionNumber() == 2.0f &&
                    !resp.getVersionsList().get(1).getCurrentVersion();
        }));

        verify(responseObserver).onCompleted();
    }

    @Test
    void getSopVersions_Error() {
        // Arrange
        String sopId = "test-sop-id";
        GetSopVersionsRequest request = GetSopVersionsRequest.newBuilder()
                .setSopId(sopId)
                .build();

        String errorMessage = "Database error";
        when(sopVersionService.getSopVersions(sopId)).thenThrow(new RuntimeException(errorMessage));

        // Act
        versionGrpcService.getSopVersions(request, responseObserver);

        // Assert
        verify(sopVersionService).getSopVersions(sopId);

        verify(responseObserver).onNext(argThat(response -> {
            GetSopVersionsResponse resp = (GetSopVersionsResponse) response;
            return !resp.getSuccess() &&
                    resp.getErrorMessage().contains(errorMessage);
        }));

        verify(responseObserver).onCompleted();
    }

    @Test
    void getSopVersions_EmptyList() {
        // Arrange
        String sopId = "test-sop-id";
        GetSopVersionsRequest request = GetSopVersionsRequest.newBuilder()
                .setSopId(sopId)
                .build();

        when(sopVersionService.getSopVersions(sopId)).thenReturn(List.of());

        // Act
        versionGrpcService.getSopVersions(request, responseObserver);

        // Assert
        verify(sopVersionService).getSopVersions(sopId);

        verify(responseObserver).onNext(argThat(response -> {
            GetSopVersionsResponse resp = (GetSopVersionsResponse) response;
            return resp.getSuccess() &&
                    resp.getVersionsList().isEmpty();
        }));

        verify(responseObserver).onCompleted();
    }
}