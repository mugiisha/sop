package com.notification_service.notification_service.controllers;

import com.notification_service.notification_service.dtos.UpdateNotificationPreferencesDto;
import com.notification_service.notification_service.models.NotificationPreferences;
import com.notification_service.notification_service.services.NotificationPreferencesService;
import com.notification_service.notification_service.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping
    public Response<NotificationPreferences> getUserNotificationPreferences(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        NotificationPreferences preferences = notificationPreferencesService.getUserNotificationPreferences(UUID.fromString(userId));

        return new Response<>(true,
                "User notification preferences retrieved successfully",
                preferences);
    }

    @PostMapping
    public Response<NotificationPreferences> createNotificationPreferences(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        notificationPreferencesService.createNotificationPreferences(UUID.fromString(userId), email);

        return new Response<>(true,
                "User notification preferences created successfully",
                null);
    }
    @PutMapping
    public Response<NotificationPreferences> updateNotificationPreferences(HttpServletRequest request,
                                                                          @RequestBody UpdateNotificationPreferencesDto updateNotificationPreference) {

        String userId = request.getHeader("X-User-Id");
        NotificationPreferences preferences = notificationPreferencesService
                .updateNotificationPreferences(UUID.fromString(userId), updateNotificationPreference);

        return new Response<>(true,
                "User notification preferences updated successfully",
                preferences);
    }
}
