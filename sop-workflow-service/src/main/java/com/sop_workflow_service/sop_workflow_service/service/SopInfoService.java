package com.sop_workflow_service.sop_workflow_service.service;

import com.google.protobuf.Empty;
import com.sop_workflow_service.grpc.*;
import com.sop_workflow_service.grpc.SopWorkflowProto.*;
import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class SopInfoService extends SopWorkflowServiceGrpc.SopWorkflowServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SopInfoService.class);

    @Autowired
    private SOPService SopService;

    @Override
    public void getAllSops(Empty request, StreamObserver<SopListResponse> responseObserver) {
        logger.info("Received request to fetch all SOPs.");

        try {
            // Fetch all SOPs using the SopService
            logger.debug("Fetching all SOPs using SopService.");
            List<SOP> sopModels = SopService.getAllSops();
            logger.debug("Fetched {} SOPs from SopService.", sopModels.size());

            // Map SopModel to Sop (gRPC object)
            logger.debug("Mapping SopModel objects to Sop gRPC objects.");
            List<Sop> sops = sopModels.stream()
                    .map(sopModel -> {
                        logger.trace("Mapping SOP: {}", sopModel);
                        return Sop.newBuilder()
                                .setId(sopModel.getId())
                                .setTitle(sopModel.getTitle() != null ? sopModel.getTitle() : "")
//                                .setStatus(sopModel.getStatus() != null ? sopModel.getStatus() : "")
                                .setDepartmentId(sopModel.getDepartmentId() != null ? String.valueOf(sopModel.getDepartmentId()) : "")
//                                .setVisibility(sopModel.getVisibility() != null ? sopModel.getVisibility() : "")
                                .setCategory(sopModel.getCategory() != null ? sopModel.getCategory() : "")
                                .setCreatedAt(sopModel.getCreatedAt() != null ? com.google.protobuf.Timestamp.newBuilder().setSeconds(sopModel.getCreatedAt().getEpochSecond()).build() : com.google.protobuf.Timestamp.getDefaultInstance())
                                .setUpdatedAt(sopModel.getUpdatedAt() != null ? com.google.protobuf.Timestamp.newBuilder().setSeconds(sopModel.getUpdatedAt().getEpochSecond()).build() : com.google.protobuf.Timestamp.getDefaultInstance())
                                .build();
                    })
                    .collect(Collectors.toList());
            logger.debug("Mapping completed. Total SOPs mapped: {}", sops.size());

            // Build and send the response
            SopListResponse response = SopListResponse.newBuilder()
                    .addAllSops(sops)
                    .build();
            logger.debug("Response built successfully: {}", response);

            responseObserver.onNext(response);
            logger.info("Response sent successfully.");
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle any errors
            logger.error("Error occurred while processing getAllSops request: ", e);
            responseObserver.onError(e);
        }
    }
}