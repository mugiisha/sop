package com.user_management_service.user_management_service.dtos;
import lombok.Data;
import java.util.UUID;

@Data
public class NotificationPreferenceDTO {
    private UUID userId;
    private boolean emailEnabled;
    private boolean inAppEnabled;
}