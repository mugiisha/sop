package com.sop_workflow_service.sop_workflow_service.dto;
import lombok.Data;
import java.util.List;

@Data
public class SOPRequestDto {
    private String title;
    private String description;
    private String version;
    private String status; // DRAFT, REVIEW, APPROVAL
    private List<String> authorIds; // List of author IDs
    private List<String> reviewerIds; // List of reviewer IDs
    private List<String> approverIds; // List of approver IDs
}
