package com.version_control_service.version_control_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "sop")
public class SOP {
    @Id
    private String id;
    @DBRef
    private List<Version> versions;
}
