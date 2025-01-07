package com.sop_recommendation_service.sop_recommendation_service.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import sopFromWorkflow.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SopClientService {

    @GrpcClient("sop-content-service")
    private SopServiceGrpc.SopServiceBlockingStub sopServiceStub;

    public GetSopDetailsResponse getSopDetails(String sopId) {
        try {
            log.info("Sending gRPC request to get SOP details for sopId: {}", sopId);
            GetSopDetailsRequest request = GetSopDetailsRequest.newBuilder()
                    .setSopId(sopId)
                    .build();

            GetSopDetailsResponse response = sopServiceStub
                    .withDeadlineAfter(10, TimeUnit.SECONDS)
                    .getSopDetails(request);
            log.info("Received response from sop-content-service for sopId: {}", sopId);

            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed while getting SOP details: status={}, message={}", e.getStatus(), e.getMessage());

            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                log.error("Unable to connect to sop-content-service. Please check if the service is running and accessible.");
            }

            return GetSopDetailsResponse.newBuilder()
                    .setId("")
                    .addAllDocumentUrls(Collections.emptyList())
                    .setCoverUrl("")
                    .setTitle("")
                    .setDescription("")
                    .setBody("")
                    .setCategory("")
                    .setDepartmentId("")
                    .setVisibility("")
                    .setStatus("")
                    .addAllVersions(Collections.emptyList())
                    .addAllReviewers(Collections.emptyList())
                    .setApprover(Stage.newBuilder()
                            .setName("")
                            .setProfilePictureUrl("")
                            .setStatus("")
                            .build())
                    .setAuthor(Stage.newBuilder()
                            .setName("")
                            .setProfilePictureUrl("")
                            .setStatus("")
                            .build())
                    .setCreatedAt("")
                    .setUpdatedAt("")
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error while getting SOP details", e);
            return createEmptySopDetailsResponse();
        }
    }

    public GetAllSopDetailsResponse getAllSopDetails() {
        try {
            log.info("Sending gRPC request to get all SOP details");
            GetAllSopDetailsRequest request = GetAllSopDetailsRequest.newBuilder()
                    .build();

            GetAllSopDetailsResponse response = sopServiceStub
                    .withDeadlineAfter(30, TimeUnit.SECONDS)
                    .getAllSopDetails(request);
            log.info("Received response from sop-content-service for all SOPs");

            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed while getting all SOP details: status={}, message={}", e.getStatus(), e.getMessage());

            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                log.error("Unable to connect to sop-content-service. Please check if the service is running and accessible.");
            }

            return GetAllSopDetailsResponse.newBuilder()
                    .addAllSopDetails(Collections.emptyList())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error while getting all SOP details", e);
            return GetAllSopDetailsResponse.newBuilder()
                    .addAllSopDetails(Collections.emptyList())
                    .build();
        }
    }

    private GetSopDetailsResponse createEmptySopDetailsResponse() {
        return GetSopDetailsResponse.newBuilder()
                .setId("")
                .addAllDocumentUrls(Collections.emptyList())
                .setCoverUrl("")
                .setTitle("")
                .setDescription("")
                .setBody("")
                .setCategory("")
                .setDepartmentId("")
                .setVisibility("")
                .setStatus("")
                .addAllVersions(Collections.emptyList())
                .addAllReviewers(Collections.emptyList())
                .setApprover(Stage.newBuilder()
                        .setName("")
                        .setProfilePictureUrl("")
                        .setStatus("")
                        .build())
                .setAuthor(Stage.newBuilder()
                        .setName("")
                        .setProfilePictureUrl("")
                        .setStatus("")
                        .build())
                .setCreatedAt("")
                .setUpdatedAt("")
                .build();
    }
}