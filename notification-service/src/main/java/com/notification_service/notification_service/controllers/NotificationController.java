package com.notification_service.notification_service.controllers;

import com.notification_service.notification_service.dtos.CreateNotificationDto;
import com.notification_service.notification_service.models.Notification;
import com.notification_service.notification_service.services.NotificationService;
import com.notification_service.notification_service.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Response<List<Notification>> getNotificationsByUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        log.info("Retrieving notifications for user with id: {}", userId);

        List<Notification> notifications = notificationService.getNotificationsByUserId(UUID.fromString(userId));

        return new Response<>(true, "Notifications retrieved successfully", notifications);
    }

    @PostMapping
    public Notification createNotification(@RequestBody CreateNotificationDto notification) {
        log.info("Creating notification");

        return notificationService.createNotification(notification);
    }

    @GetMapping("/{id}")
    public Notification getNotificationById(@PathVariable String id) {
        log.info("Retrieving notification with id: {}", id);

        return notificationService.getNotificationById(id);
    }

}
