package com.audit_compliance_tracking_service.audit_compliance_tracking_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcknowledgedUser {
    private String userId; // ID of the user
    private String name; // Name of the user
}
