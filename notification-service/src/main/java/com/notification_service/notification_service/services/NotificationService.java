package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.CreateNotificationDto;
import com.notification_service.notification_service.dtos.CustomUserDto;
import com.notification_service.notification_service.models.Notification;
import com.notification_service.notification_service.repositories.NotificationRepository;
import com.notification_service.notification_service.utils.DtoConverter;
import com.notification_service.notification_service.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

   private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final NotificationPreferencesService notificationPreferencesService;

    public Notification createNotification(CreateNotificationDto createNotificationDto) {
        log.info("Creating notification");

        Notification notification = new Notification();
        notification.setUserId(createNotificationDto.getUserId());
        notification.setMessage(createNotificationDto.getMessage());
        notification.setRead(createNotificationDto.isRead());
        notification.setType(createNotificationDto.getType());
        notification.setSopId(createNotificationDto.getSopId());
        notification.setCreatedAt(LocalDateTime.now().toString());

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(UUID userId) throws NotFoundException {
        log.info("Retrieving notification for user with id: {}", userId);

        return notificationRepository.findAllByUserId(userId);
    }


    public Notification getNotificationById(String notificationId) throws NotFoundException {
        log.info("Retrieving notification with id: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Error retrieving notification with id: {}", notificationId);
                    return new NotFoundException("Notification not found");
                });

        notification.setRead(true);
        notificationRepository.save(notification);

        return notification;
    }


    public void sendNotification(UUID userId, Notification notification) {
        log.info("Sending notification to user with id: {}, payload: {}", userId, notification);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification);
    }


    @KafkaListener(
            topics = "user-created",
            groupId = "notification-service"
    )

    void userCreatedListener(String data) {
        try {
            log.info("Received user created event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            // Create default notification preferences for the created User
            notificationPreferencesService.createNotificationPreferences(
                    customUserDto.getId(),
                    customUserDto.getEmail());

            emailService.sendVerificationEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getVerificationToken());
        } catch (Exception e) {
            log.error("Failed to send user created email: {}", data, e);
        }
    }


    @KafkaListener(
            topics = "user-deactivated",
            groupId = "notification-service"
    )
    void userDeactivatedListener(String data) {
        try {
            log.info("Received user deactivated event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            emailService.sendAccountDeactivationEmail(customUserDto.getEmail(), customUserDto.getName());
        } catch (Exception e) {
            log.error("Failed to send user deactivated email: {}", data, e);
        }
    }


    @KafkaListener(
            topics = "user-locked",
            groupId = "notification-service"
    )
    void userLockedListener(String data) {
        try {
            log.info("Received user deactivated event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            emailService.sendAccountLockedEmail(customUserDto.getEmail(), customUserDto.getName());
        } catch (Exception e) {
            log.error("Failed to send user deactivated email: {}", data, e);
        }
    }


    @KafkaListener(
            topics = "otp-request",
            groupId = "notification-service"
    )
    void otpRequestListener(String data) {
        try {
            log.info("Received user deactivated event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            emailService.sendOtpEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getPassword());
        } catch (Exception e) {
            log.error("Failed to send user deactivated email: {}", data, e);
        }
    }

    @KafkaListener(
            topics = "password-updated",
            groupId = "notification-service"
    )
    void passwordUpdatedListener(String data) {
        try {
            log.info("Received user deactivated event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            emailService.sendPasswordChangeConfirmation(customUserDto.getEmail(), customUserDto.getName());
        } catch (Exception e) {
            log.error("Failed to send user deactivated email: {}", data, e);
        }
    }


    @KafkaListener(
            topics = "email-verified",
            groupId = "notification-service"
    )
    void emailVerifiedListener(String data) {
        try {
            log.info("Received user deactivated event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            emailService.sendVerificationConfirmationEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getPassword());
        } catch (Exception e) {
            log.error("Failed to send user deactivated email: {}", data, e);
        }
    }

}