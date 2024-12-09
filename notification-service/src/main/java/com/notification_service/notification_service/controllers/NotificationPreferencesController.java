package com.notification_service.notification_service.controllers;

import com.notification_service.notification_service.dtos.UpdateNotificationPreferencesDto;
import com.notification_service.notification_service.models.NotificationPreferences;
import com.notification_service.notification_service.services.NotificationPreferencesService;
import com.notification_service.notification_service.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
public class NotificationPreferencesController {

    private final NotificationPreferencesService notificationPreferencesService;

    @Autowired
    public NotificationPreferencesController(NotificationPreferencesService notificationPreferencesService) {
        this.notificationPreferencesService = notificationPreferencesService;
    }

    @GetMapping("/{userId}")
    public Response<NotificationPreferences> getUserNotificationPreferences(@PathVariable UUID userId) {

        NotificationPreferences preferences = notificationPreferencesService.getUserNotificationPreferences(userId);

        return new Response<>(true,
                "User notification preferences retrieved successfully",
                preferences);
    }

    @PutMapping("/{userId}")
    public Response<NotificationPreferences> updateNotificationPreferences(@PathVariable UUID userId,
                                                                          @RequestBody UpdateNotificationPreferencesDto updateNotificationPreference) {

        NotificationPreferences preferences = notificationPreferencesService
                .updateNotificationPreferences(userId, updateNotificationPreference);

        return new Response<>(true,
                "User notification preferences updated successfully",
                preferences);
    }
}
