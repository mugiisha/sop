package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.model.SopModel;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sopContentService.*;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class SopGrpcService extends SopContentServiceGrpc.SopContentServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SopGrpcService.class);

    @Autowired
    private SopService sopService;

    @Override
    public void getAllSops(Empty request, StreamObserver<SopListResponse> responseObserver) {
        logger.info("Received request to fetch all SOPs.");

        try {
            // Fetch all SOPs using the SopService
            logger.debug("Fetching all SOPs using SopService.");
            List<SopModel> sopModels = sopService.getAllSOPs().getBody().getData();
            logger.debug("Fetched {} SOPs from SopService.", sopModels.size());

            // Map SopModel to Sop (gRPC object)
            logger.debug("Mapping SopModel objects to Sop gRPC objects.");
            List<Sop> sops = sopModels.stream()
                    .map(sopModel -> {
                        logger.trace("Mapping SOP: {}", sopModel);
                        return Sop.newBuilder()
                                .setId(sopModel.getId())
                                .setTitle(sopModel.getTitle() != null ? sopModel.getTitle() : "")
                                .setDescription(sopModel.getDescription() != null ? sopModel.getDescription() : "")
                                .setStatus(sopModel.getStatus() != null ? sopModel.getStatus() : "")
                                .setVersion(sopModel.getVersion() != null ? sopModel.getVersion() : "")
                                .setImageUrl(sopModel.getImageUrl() != null ? sopModel.getImageUrl() : "")
                                .setDocumentUrl(sopModel.getDocumentUrl() != null ? sopModel.getDocumentUrl() : "")
                                .addAllApprovers(sopModel.getApprovers() != null ? sopModel.getApprovers() : List.of())
                                .addAllReviewers(sopModel.getReviewers() != null ? sopModel.getReviewers() : List.of())
                                .addAllAuthors(sopModel.getAuthors() != null ? sopModel.getAuthors() : List.of())
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
