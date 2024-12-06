package com.notification_service.notification_service.models;

import com.notification_service.notification_service.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "notifications")
public class Notification {
    private String id;
    private UUID userId;
    private String message;
    private boolean isRead;
    private NotificationType type;
    private String sopId;
    private String sopTitle;
    private String createdAt;
    private String updatedAt;
}
