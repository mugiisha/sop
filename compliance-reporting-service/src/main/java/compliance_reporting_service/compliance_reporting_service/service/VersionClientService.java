package compliance_reporting_service.compliance_reporting_service.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import sopVersionService.*;

@Service
public class VersionClientService {

    @GrpcClient("version-control-service")
    VersionServiceGrpc.VersionServiceBlockingStub versionServiceBlockingStub;


    public GetSopVersionsResponse GetSopVersions(String sopId){
        return versionServiceBlockingStub.getSopVersions(
                GetSopVersionsRequest
                        .newBuilder()
                        .setSopId(sopId)
                        .build()
        );
    }
}
