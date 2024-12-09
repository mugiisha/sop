package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.model.SopModel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import sopworkflowservice.SopWorkflowServiceGrpc;
import sopworkflowservice.Sopworkflowservice;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SopWorkflowServiceClient {

    @GrpcClient("sop-workflow-service")
    private SopWorkflowServiceGrpc.SopWorkflowServiceBlockingStub blockingStub;

    public List<Sopworkflowservice.SOPListResponse> getAllSOPs() {
        // Create an Empty request
        Sopworkflowservice.Empty request = Sopworkflowservice.Empty.newBuilder().build();

        // Invoke the getAllSOPs method on the server
        Sopworkflowservice.SOPListResponse response = blockingStub.getAllSOPs(request);

        // Return the list of SOPs from the response
        return response.getSopsList();
    }

    public void printAllSOPs() {
        // Fetch all SOPs
        List<Sopworkflowservice.SOP> sops = getAllSOPs();

        // Print the details of each SOP
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC);

        for (Sopworkflowservice.SOP sop : sops) {
            System.out.println("SOP Details:");
            System.out.println("ID: " + sop.getId());
            System.out.println("Title: " + sop.getTitle());
            System.out.println("Status: " + sop.getStatus());
            System.out.println("Department ID: " + sop.getDepartmentId());
            System.out.println("Visibility: " + sop.getVisibility());
            System.out.println("Category: " + sop.getCategory());
            System.out.println("Created At: " + (sop.hasCreatedAt() ?
                    formatter.format(Instant.ofEpochSecond(sop.getCreatedAt().getSeconds())) : "N/A"));
            System.out.println("Updated At: " + (sop.hasUpdatedAt() ?
                    formatter.format(Instant.ofEpochSecond(sop.getUpdatedAt().getSeconds())) : "N/A"));
            System.out.println("-----");
        }
    }
}
