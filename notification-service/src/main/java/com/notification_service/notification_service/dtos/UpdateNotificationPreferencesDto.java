package com.notification_service.notification_service.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateNotificationPreferencesDto {
    // True is the default value
    private boolean allSOPAlertsEnabled = true;
    private boolean authorAlertsEnabled = true;
    private boolean reviewerAlertsEnabled = true;
    private boolean approverAlertsEnabled = true;
}
