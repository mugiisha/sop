package com.version_control_service.version_control_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "sop_version")
public class Version {
    @Id
    private String id;
    private float versionNumber;
    private String sopId;
    private boolean currentVersion;
    @DBRef
    private VersionContent content;
}
