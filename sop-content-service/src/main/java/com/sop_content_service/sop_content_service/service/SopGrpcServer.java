package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.dto.SopVersionDto;
import com.sop_content_service.sop_content_service.dto.StageDto;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import sopFromWorkflow.*;
import com.sop_content_service.sop_content_service.dto.SOPResponseDto;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class SopGrpcServer extends SopServiceGrpc.SopServiceImplBase {

    private final SopService sopService;

    @Override
    public void getSopDetails(GetSopDetailsRequest request, StreamObserver<GetSopDetailsResponse> responseObserver) {
        try {
            log.info("Received gRPC request for SOP details. SOP ID: {}", request.getSopId());

            SOPResponseDto sop = sopService.getSopById(request.getSopId());

            GetSopDetailsResponse response = GetSopDetailsResponse.newBuilder()
                    .setId(sop.getId())
                    .addAllDocumentUrls(sop.getDocumentUrls() != null ? sop.getDocumentUrls() : Collections.emptyList())
                    .setCoverUrl(sop.getCoverUrl() != null ? sop.getCoverUrl() : "")
                    .setTitle(sop.getTitle())
                    .setDescription(sop.getDescription() != null ? sop.getDescription() : "")
                    .setBody(sop.getBody() != null ? sop.getBody() : "")
                    .setCategory(sop.getCategory())
                    .setDepartmentId(sop.getDepartmentId().toString())
                    .setVisibility(sop.getVisibility().toString())
                    .setStatus(sop.getStatus().toString())
                    .addAllVersions(mapVersions(sop.getVersions()))
                    .addAllReviewers(mapStages(sop.getReviewers()))
                    .setApprover(mapStage(sop.getApprover()))
                    .setAuthor(mapStage(sop.getAuthor()))
                    .setCreatedAt(sop.getCreatedAt().toString())
                    .setUpdatedAt(sop.getUpdatedAt().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully sent SOP details for ID: {}", request.getSopId());

        } catch (Exception e) {
            log.error("Error processing getSopDetails request for ID: {}", request.getSopId(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error retrieving SOP details: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getAllSopDetails(GetAllSopDetailsRequest request, StreamObserver<GetAllSopDetailsResponse> responseObserver) {
        try {
            log.info("Received gRPC request for all SOP details");

            List<SOPResponseDto> sops = sopService.getAllSops();

            List<SopDetails> sopDetailsList = sops.stream()
                    .map(this::mapToSopDetails)
                    .collect(Collectors.toList());

            GetAllSopDetailsResponse response = GetAllSopDetailsResponse.newBuilder()
                    .addAllSopDetails(sopDetailsList)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully sent details for {} SOPs", sopDetailsList.size());

        } catch (Exception e) {
            log.error("Error processing getAllSopDetails request", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error retrieving all SOP details: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    private List<SopVersion> mapVersions(List<SopVersionDto> versions) {
        if (versions == null) {
            return Collections.emptyList();
        }

        return versions.stream()
                .map(version -> SopVersion.newBuilder()
                        .setVersionNumber(version.getVersionNumber())
                        .setCurrentVersion(version.isCurrentVersion())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Stage> mapStages(List<StageDto> stages) {
        if (stages == null) {
            return Collections.emptyList();
        }

        return stages.stream()
                .map(this::mapStage)
                .collect(Collectors.toList());
    }

    private Stage mapStage(StageDto stage) {
        if (stage == null) {
            return Stage.newBuilder()
                    .setName("")
                    .setProfilePictureUrl("")
                    .setStatus("UNKNOWN")
                    .build();
        }

        Stage.Builder stageBuilder = Stage.newBuilder()
                .setName(stage.getName() != null ? stage.getName() : "")
                .setProfilePictureUrl(stage.getProfilePictureUrl() != null ? stage.getProfilePictureUrl() : "")
                .setStatus(stage.getStatus() != null ? stage.getStatus().toString() : "UNKNOWN");

        if (stage.getComments() != null) {
            List<Comment> comments = stage.getComments().stream()
                    .map(comment -> Comment.newBuilder()
                            .setCommentId(comment.getCommentId())
                            .setComment(comment.getComment())
                            .setCreatedAt(comment.getCreatedAt().toString())
                            .build())
                    .collect(Collectors.toList());
            stageBuilder.addAllComments(comments);
        }

        return stageBuilder.build();
    }

    private SopDetails mapToSopDetails(SOPResponseDto sop) {
        return SopDetails.newBuilder()
                .setSopId(sop.getId())
                .setTitle(sop.getTitle())
                .setDescription(sop.getDescription() != null ? sop.getDescription() : "")
                .setBody(sop.getBody() != null ? sop.getBody() : "")
                .setCategory(sop.getCategory())
                .setDepartmentId(sop.getDepartmentId().toString())
                .setVisibility(sop.getVisibility().toString())
                .setStatus(sop.getStatus().toString())
                .addAllDocumentUrls(sop.getDocumentUrls() != null ? sop.getDocumentUrls() : Collections.emptyList())
                .setCoverUrl(sop.getCoverUrl() != null ? sop.getCoverUrl() : "")
                .setInitiatedBy(sop.getAuthor() != null ? sop.getAuthor().getName() : "Unknown")
                .setCreatedAt(sop.getCreatedAt().toString())
                .setUpdatedAt(sop.getUpdatedAt().toString())
                .build();
    }
}