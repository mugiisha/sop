package com.version_control_service.version_control_service.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "sop_versions")
@Data
@NoArgsConstructor
public class SopVersionModel {

    @Id
    private String id;

    @NotBlank(message = "SOP ID cannot be blank")
    @Size(max = 50, message = "SOP ID must not exceed 50 characters")
    private String sopId;

    @NotBlank(message = "Version number cannot be blank")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)*$", message = "Version number must follow a valid format, e.g., '1.0' or '1.1.2'")
    @Size(max = 20, message = "Version number must not exceed 20 characters")
    private String versionNumber;

    @NotBlank(message = "Created by cannot be blank")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @CreatedDate
    @Field("createdAt") // Ensure the field name matches the MongoDB collection name
    private Date createdAt;

    public SopVersionModel(String sopId, String versionNumber, String createdBy, String description) {
        this.sopId = sopId;
        this.versionNumber = versionNumber;
        this.createdBy = createdBy;
        this.description = description;

    }
}
