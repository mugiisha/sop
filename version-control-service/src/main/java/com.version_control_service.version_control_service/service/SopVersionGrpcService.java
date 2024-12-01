package com.version_control_service.version_control_service.service;

import com.version_control_service.version_control_service.dto.SopDto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import sopContentService.Empty;
import sopContentService.SopContentServiceGrpc;
import sopContentService.SopListResponse;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SopVersionGrpcService {

    @GrpcClient("sop-content-service")
    private SopContentServiceGrpc.SopContentServiceBlockingStub sopContentServiceBlockingStub;

    public List<SopDto> getAllSops() {
        // Make gRPC call to fetch all SOPs
        SopListResponse sopListResponse = sopContentServiceBlockingStub.getAllSops(Empty.newBuilder().build());

        // Convert gRPC Sop objects to DTOs
        return sopListResponse.getSopsList().stream()
                .map(sop -> new SopDto(
                        sop.getId(),
                        sop.getTitle(),
                        sop.getDescription(),
                        sop.getStatus(),
                        sop.getVersion(),
                        sop.getImageUrl(),
                        sop.getDocumentUrl(),
                        sop.getCode(),
                        sop.getVisibility(),
                        sop.getCategory()
                ))
                .collect(Collectors.toList());
    }
}
