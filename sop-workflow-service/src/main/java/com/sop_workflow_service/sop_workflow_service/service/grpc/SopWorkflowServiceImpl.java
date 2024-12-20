package com.sop_workflow_service.sop_workflow_service.service.grpc;

import com.sop_workflow_service.sop_workflow_service.model.SOP;
import com.sop_workflow_service.sop_workflow_service.repository.SOPRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import sopFromWorkflow.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Server implementation for the SOP workflow gRPC service.
 */
@RequiredArgsConstructor
@GrpcService
public class SopWorkflowServiceImpl extends SopServiceGrpc.SopServiceImplBase {

    private final SOPRepository sopRepository;

    @Override
    public void getSopDetails(GetSopDetailsRequest request, StreamObserver<GetSopDetailsResponse> responseObserver) {
        String sopId = request.getSopId();
        try {
            // Fetch SOP from the database
            SOP sop = sopRepository.findById(sopId)
                    .orElseThrow(() -> new RuntimeException("SOP not found for ID: " + sopId));

            // Build the response
            GetSopDetailsResponse response = GetSopDetailsResponse.newBuilder()
                    .setTitle(sop.getTitle())
                    .setVisibility(String.valueOf(sop.getVisibility()))
                    .setInitiationDate(sop.getCreatedAt().toString())
                    .setCompletionDate(sop.getUpdatedAt().toString())
                    .build();

            responseObserver.onNext(response);
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getAllSopDetails(GetAllSopDetailsRequest request, StreamObserver<GetAllSopDetailsResponse> responseObserver) {
        String filter = request.getFilter();

        try {
            // Fetch all SOPs from the database
            List<SOP> sops = sopRepository.findAll(); // Apply filtering logic if needed

            // Build the response
            List<SopDetails> sopDetailsList = sops.stream()
                    .map(sop -> SopDetails.newBuilder()
                            .setSopId(sop.getId())
                            .setTitle(sop.getTitle())
                            .setVisibility(String.valueOf(sop.getVisibility()))
                            .setInitiationDate(sop.getCreatedAt().toString())
                            .setCompletionDate(sop.getUpdatedAt().toString())
                            .build())
                    .collect(Collectors.toList());

            GetAllSopDetailsResponse response = GetAllSopDetailsResponse.newBuilder()
                    .addAllSopDetails(sopDetailsList)
                    .build();

            responseObserver.onNext(response);
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }
}
