package com.notification_service.notification_service.dtos;

import com.notification_service.notification_service.enums.NotificationType;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateNotificationDto {
    private String id;
    private UUID userId;
    private String message;
    private NotificationType type;
    private String sopId;
    private String sopTitle;
}
