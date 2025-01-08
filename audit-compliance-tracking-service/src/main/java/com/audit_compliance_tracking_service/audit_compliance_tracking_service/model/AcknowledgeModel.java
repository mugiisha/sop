package com.audit_compliance_tracking_service.audit_compliance_tracking_service.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "acknowledged") // Specifies the MongoDB collection name
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
public class AcknowledgeModel {
    @Id
    private String id; // MongoDB's unique identifier

    @NotBlank(message = "SOP ID cannot be blank")
    private String sopId; // ID of the associated SOP

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "departmentId cannot be blank")
    private String departmentId;

    @NotBlank(message = "status cannot be blank")
    private String status;

    @NotBlank(message = "initiatedBy cannot be blank")
    private String initiatedBy;

    @CreatedDate
    private Date timestamp; // Automatically populated with the creation timestamp

    private List<String> acknowledgedBy; // List of users who acknowledged the SOP
}