package com.version_control_service.version_control_service.service;

import com.version_control_service.version_control_service.dto.SopVersionDto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sopVersionService.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@GrpcService
public class VersionGrpcService extends VersionServiceGrpc.VersionServiceImplBase{
    private final SopVersionService sopVersionService;

    @Autowired
    public VersionGrpcService(SopVersionService sopVersionService) {
        this.sopVersionService = sopVersionService;
    }

    @Override
    public void getSopVersions(GetSopVersionsRequest request, StreamObserver<GetSopVersionsResponse> responseObserver) {
       try {
           log.info("Fetching SOP versions by SOP id");
           String sopId = request.getSopId();

           List<SopVersionDto> sopVersions = sopVersionService.getSopVersions(sopId);

           List<SopVersion> versions = new ArrayList<>();

           for (SopVersionDto sopVersion : sopVersions) {
               SopVersion version = SopVersion.newBuilder()
                       .setVersionNumber(sopVersion.getVersionNumber())
                       .setCurrentVersion(sopVersion.getCurrentVersion())
                       .build();
               versions.add(version);
           }

           GetSopVersionsResponse response = GetSopVersionsResponse.newBuilder()
                   .setSuccess(true)
                   .addAllVersions(versions)
                   .build();

           responseObserver.onNext(response);
           responseObserver.onCompleted();
       } catch (Exception e) {
           log.error("Error fetching SOP versions by SOP id: ", e);
           GetSopVersionsResponse response = GetSopVersionsResponse.newBuilder()
                   .setSuccess(false)
                   .setErrorMessage("Failed to get SOP versions by SOP id: " + e.getMessage())
                   .build();
           responseObserver.onNext(response);
           responseObserver.onCompleted();
       }
    }
}
