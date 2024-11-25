package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.model.SopModel;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sopContentService.*;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class SopGrpcService extends SopContentServiceGrpc.SopContentServiceImplBase{

    @Autowired
    private SopService sopService;

    @Override
    public void getAllSops(Empty request, StreamObserver<SopListResponse> responseObserver) {
        try {
            // Fetch all SOPs using the SopService
            List<SopModel> sopModels = sopService.getAllSOPs().getBody().getData();

            // Map SopModel to Sop (gRPC object)
            List<Sop> sops = sopModels.stream()
                    .map(sopModel -> Sop.newBuilder()
                            .setId(sopModel.getId())
                            .setTitle(sopModel.getTitle())
                            .setDescription(sopModel.getDescription())
                            .setStatus(sopModel.getStatus())
                            .setVersion(sopModel.getVersion())
                            .setImageUrl(sopModel.getImageUrl() != null ? sopModel.getImageUrl() : "")
                            .setDocumentUrl(sopModel.getDocumentUrl() != null ? sopModel.getDocumentUrl() : "")
                            .build())
                    .collect(Collectors.toList());

            // Build and send the response
            SopListResponse response = SopListResponse.newBuilder()
                    .addAllSops(sops)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle any errors
            responseObserver.onError(e);
        }
    }

}
