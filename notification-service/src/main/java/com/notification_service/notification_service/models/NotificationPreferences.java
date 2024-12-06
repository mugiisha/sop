package com.notification_service.notification_service.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "notification_preferences")
public class NotificationPreferences {
    private String id;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private UUID userId;
    private boolean emailEnabled;
    private boolean inAppEnabled;
}
