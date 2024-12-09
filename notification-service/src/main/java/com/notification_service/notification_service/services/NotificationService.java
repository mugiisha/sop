package com.notification_service.notification_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.notification_service.notification_service.dtos.CreateNotificationDto;
import com.notification_service.notification_service.dtos.CustomUserDto;
import com.notification_service.notification_service.dtos.SOPDto;
import com.notification_service.notification_service.enums.NotificationType;
import com.notification_service.notification_service.models.Notification;
import com.notification_service.notification_service.repositories.NotificationRepository;
import com.notification_service.notification_service.utils.DtoConverter;
import com.notification_service.notification_service.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public Notification createAndSendNotification(CreateNotificationDto createNotificationDto) {
        log.info("Creating notification");
        UUID userId = createNotificationDto.getUserId();

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(createNotificationDto.getMessage());
        notification.setType(createNotificationDto.getType());
        notification.setSopId(createNotificationDto.getSopId());
        notification.setCreatedAt(LocalDateTime.now().toString());

        notificationRepository.save(notification);

        log.info("Sending notification to user with id: {}, payload: {}", userId, notification);

        // Send real time notification to user
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification);

        return notification;
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


    @KafkaListener(
            topics = "sop-created",
            groupId = "notification-service"
    )
    void sopCreatedListener(String data) {
        try {
            log.info("Received sop created event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID authorId = sopDto.getAuthorId();
            UUID approverId = sopDto.getApproverId();

            // Check author's preferences before we create and send notification for the author
            if (notificationPreferencesService.isAllSOPAlertsEnabled(authorId) || notificationPreferencesService.isAuthorAlertsEnabled(authorId)) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        authorId,
                        NotificationType.SOP_ASSIGNED_AS_AUTHOR,
                        "you have been assigned as an author on a newly initiated SOP " + sopDto.getTitle(),
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
            }

            // Loop through reviewers,Check reviewer's preferences before we create and send notifications for each
            for (UUID reviewerId : sopDto.getReviewers()) {
                if (notificationPreferencesService.isAllSOPAlertsEnabled(reviewerId) || notificationPreferencesService.isReviewerAlertsEnabled(reviewerId)) {
                    CreateNotificationDto notificationDto = prepareNotificationDto(
                            reviewerId,
                            NotificationType.SOP_ASSIGNED_AS_REVIEWER,
                            "You have been assigned as a reviewer on a newly initiated SOP: " + sopDto.getTitle(),
                            sopDto.getId(),
                            sopDto.getTitle()
                    );

                    createAndSendNotification(notificationDto);
                }
            }

            // Create and send notification for the approver
            if(notificationPreferencesService.isAllSOPAlertsEnabled(approverId) || notificationPreferencesService.isApproverAlertsEnabled(approverId)) {
                CreateNotificationDto approverNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_ASSIGNED_AS_APPROVER,
                        "You have been assigned as an approver on a newly initiated SOP: " + sopDto.getTitle(),
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(approverNotificationDto);
            }

        } catch (Exception e) {
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }


    @KafkaListener(topics = "sop-reviewed", groupId = "notification-service")
    public void sopReviewedListener(String data) {
        try{
            log.info("Received sop reviewed event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID approverId = sopDto.getApproverId();

            // Check approver's preferences before we create and send notification to the author
            if(notificationPreferencesService.isAllSOPAlertsEnabled(approverId) || notificationPreferencesService.isApproverAlertsEnabled(approverId)) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_APPROVAL_REQUIRED,
                        "an a SOP titled" + sopDto.getTitle() + " you are assigned have been reviewed by all assigned reviewers and needs your approval",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
            }

        }catch (Exception e){
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }



    @KafkaListener(
            topics = "sop-approved",
            groupId = "notification-service"
    )
    void sopApprovedListener(String data) {
        try {
            log.info("Received sop approved event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID authorId = sopDto.getAuthorId();
            UUID approverId = sopDto.getApproverId();

            // Check author's preferences before we create and send notification for the author
            if (notificationPreferencesService.isAllSOPAlertsEnabled(authorId) || notificationPreferencesService.isAuthorAlertsEnabled(authorId)) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        authorId,
                        NotificationType.SOP_APPROVED,
                        "Your authored SOP " + sopDto.getTitle() +" has been successfully approved by all assigned users: ",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
            }

            // Loop through reviewers,Check reviewer's preferences before we create and send notifications for each
            for (UUID reviewerId : sopDto.getReviewers()) {
                if (notificationPreferencesService.isAllSOPAlertsEnabled(reviewerId) || notificationPreferencesService.isReviewerAlertsEnabled(reviewerId)) {
                    CreateNotificationDto notificationDto = prepareNotificationDto(
                            reviewerId,
                            NotificationType.SOP_APPROVED,
                            "The SOP  " + sopDto.getTitle() + " you reviewed has been approved",
                            sopDto.getId(),
                            sopDto.getTitle()
                    );

                    createAndSendNotification(notificationDto);
                }
            }

            // Create and send notification for the approver
            if(notificationPreferencesService.isAllSOPAlertsEnabled(approverId) || notificationPreferencesService.isApproverAlertsEnabled(approverId)) {
                CreateNotificationDto approverNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_APPROVED,
                        "You have approved SOP: " + sopDto.getTitle(),
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(approverNotificationDto);
            }

        } catch (Exception e) {
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }





    private CreateNotificationDto prepareNotificationDto(UUID userId, NotificationType type, String message, String sopId, String sopTitle) {
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setUserId(userId);
        notificationDto.setType(type);
        notificationDto.setMessage(message);
        notificationDto.setSopId(sopId);
        notificationDto.setSopTitle(sopTitle);
        return notificationDto;
    }

}