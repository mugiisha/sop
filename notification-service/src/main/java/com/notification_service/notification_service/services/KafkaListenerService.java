package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.CreateNotificationDto;
import com.notification_service.notification_service.dtos.CustomUserDto;
import com.notification_service.notification_service.dtos.SOPDto;
import com.notification_service.notification_service.models.Notification;
import com.notification_service.notification_service.utils.DtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class KafkaListenerService {

    private final EmailService emailService;
    private final NotificationPreferencesService notificationPreferencesService;
    private final NotificationService notificationService;
    private final Logger logger = LoggerFactory.getLogger(KafkaListenerService.class);

    @Autowired
    public KafkaListenerService(EmailService emailService,
                                NotificationPreferencesService notificationPreferencesService,
                                NotificationService notificationService) {

        this.emailService = emailService;
        this.notificationPreferencesService = notificationPreferencesService;
        this.notificationService = notificationService;
    }








    @KafkaListener(
            topics = "sop-created",
            groupId = "notification-service"
    )
    void sopCreatedListener(String data) {
        try {
            logger.info("Received sop created event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);

            // Loop through reviewers and create notifications
            for (UUID reviewerId : sopDto.getReviewers()) {
                CreateNotificationDto notificationDto = new CreateNotificationDto();
                notificationDto.setUserId(reviewerId);
                notificationDto.setMessage("A new SOP has been created: " + sopDto.getTitle());
                notificationDto.setSopId(sopDto.getId().toString());
                notificationDto.setSopTitle(sopDto.getTitle());

               Notification notification = notificationService.createNotification(notificationDto);

                notificationService.sendNotification(reviewerId, notification);
            }

            // Loop through approvers and create notifications
            for (UUID approverId : sopDto.getApprovers()) {
                CreateNotificationDto notificationDto = new CreateNotificationDto();
                notificationDto.setUserId(approverId);
                notificationDto.setMessage("A new SOP has been created: " + sopDto.getTitle());
                notificationDto.setSopId(sopDto.getId().toString());
                notificationDto.setSopTitle(sopDto.getTitle());
                Notification notification = notificationService.createNotification(notificationDto);

                notificationService.sendNotification(approverId, notification);
            }
        } catch (Exception e) {
            logger.error("Failed to create notification for sop: {}", data, e);
        }
    }

}
