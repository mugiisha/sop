package com.notification_service.notification_service.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateNotificationPreferencesDto {
    // True is the default value
    private boolean emailEnabled= true;
    private boolean inAppEnabled = true;
}
