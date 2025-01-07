package com.audit_compliance_tracking_service.audit_compliance_tracking_service.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AcknowledgedDto {
    private String id;
    private String title;
    private String departmentId;
    private String status;
    private String initiatedBy;


}
