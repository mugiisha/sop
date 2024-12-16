package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.CreateNotificationDto;
import com.notification_service.notification_service.dtos.CustomUserDto;
import com.notification_service.notification_service.dtos.SOPDto;
import com.notification_service.notification_service.enums.NotificationType;
import com.notification_service.notification_service.models.Notification;
import com.notification_service.notification_service.models.NotificationPreferences;
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
        notification.setSopTitle(createNotificationDto.getSopTitle());
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

        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
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


    @KafkaListener(topics = "user-created")
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


    @KafkaListener(topics = "user-deactivated")
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


    @KafkaListener(topics = "user-locked")
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


    @KafkaListener(topics = "otp-request")
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

    @KafkaListener(topics = "password-updated")
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


    @KafkaListener(topics = "email-verified")
    void emailVerifiedListener(String data) {
        try {
            log.info("Received email verification event: {}", data);

            // transform incoming data from user-management-service to CustomUserDto
            CustomUserDto customUserDto = DtoConverter.userDtoFromJson(data);

            emailService.sendVerificationConfirmationEmail(customUserDto.getEmail(), customUserDto.getName(), customUserDto.getPassword());
        } catch (Exception e) {
            log.error("Failed to send user deactivated email: {}", data, e);
        }
    }


    @KafkaListener(topics = "sop-created")
    void sopCreatedListener(String data) {
        try {
            log.info("Received sop created event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID authorId = sopDto.getAuthorId();
            UUID approverId = sopDto.getApproverId();

            // Check author's preferences before we create and send notification for the author
            NotificationPreferences authorPreferences = notificationPreferencesService.getUserNotificationPreferences(authorId);

            if (authorPreferences.isAllSOPAlertsEnabled() || authorPreferences.isAuthorAlertsEnabled()) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        authorId,
                        NotificationType.SOP_ASSIGNED_AS_AUTHOR,
                        "you have been assigned as an author on a newly initiated SOP " + sopDto.getTitle(),
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        authorPreferences.getEmail(),
                        "Author",
                        "Author",
                        "Assigned SOP",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-created-author"
                );
            }

            // Loop through reviewers,Check reviewer's preferences before we create and send notifications for each
            for (UUID reviewerId : sopDto.getReviewers()) {

                NotificationPreferences reviewerPreferences = notificationPreferencesService.getUserNotificationPreferences(reviewerId);

                if (reviewerPreferences.isAllSOPAlertsEnabled() || reviewerPreferences.isReviewerAlertsEnabled()) {
                    CreateNotificationDto notificationDto = prepareNotificationDto(
                            reviewerId,
                            NotificationType.SOP_ASSIGNED_AS_REVIEWER,
                            "You have been assigned as a reviewer on a newly initiated SOP: " + sopDto.getTitle(),
                            sopDto.getId(),
                            sopDto.getTitle()
                    );

                    createAndSendNotification(notificationDto);
                    emailService.sendSOPWorkflowEmail(
                            reviewerPreferences.getEmail(),
                            "Reviewer",
                            "Reviewer",
                            "Assigned SOP",
                            sopDto.getTitle(),
                            sopDto.getId(),
                            "sop-created"
                    );
                }
            }

            // Create and send notification for the approver
            NotificationPreferences approverPreferences = notificationPreferencesService.getUserNotificationPreferences(approverId);

            if(approverPreferences.isAllSOPAlertsEnabled() || approverPreferences.isApproverAlertsEnabled()) {
                CreateNotificationDto approverNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_ASSIGNED_AS_APPROVER,
                        "You have been assigned as an approver on a newly initiated SOP: " + sopDto.getTitle(),
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(approverNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        approverPreferences.getEmail(),
                        "Approver",
                        "Approver",
                        "Assigned SOP",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-created"
                );
            }

        } catch (Exception e) {
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }


    @KafkaListener(topics = "sop-reviewed")
    public void sopReviewedListener(String data) {
        try{
            log.info("Received sop reviewed event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID approverId = sopDto.getApproverId();

            // Check approver's preferences before we create and send notification to the author
            NotificationPreferences approverPreferences = notificationPreferencesService.getUserNotificationPreferences(approverId);

            if(approverPreferences.isAllSOPAlertsEnabled() || approverPreferences.isApproverAlertsEnabled()) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_APPROVAL_REQUIRED,
                        "an a SOP titled" + sopDto.getTitle() + " you are assigned have been reviewed by all assigned reviewers and needs your approval",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        approverPreferences.getEmail(),
                        "Approver",
                        "Approver",
                        "Approval Required",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-reviewed"
                );
            }

        }catch (Exception e){
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }



    @KafkaListener(topics = "sop-approved")
    void sopApprovedListener(String data) {
        try {
            log.info("Received sop approved event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID authorId = sopDto.getAuthorId();
            UUID approverId = sopDto.getApproverId();

            // Check author's preferences before we create and send notification for the author
            NotificationPreferences authorPreferences = notificationPreferencesService.getUserNotificationPreferences(authorId);

            if (authorPreferences.isAllSOPAlertsEnabled() || authorPreferences.isAuthorAlertsEnabled()) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        authorId,
                        NotificationType.SOP_APPROVED,
                        "Your authored SOP " + sopDto.getTitle() +" has been successfully approved by all assigned users: ",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        authorPreferences.getEmail(),
                        "Author",
                        "Author",
                        "SOP Approved",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-approved-author"
                );
            }

            // Loop through reviewers,Check reviewer's preferences before we create and send notifications for each
            for (UUID reviewerId : sopDto.getReviewers()) {
                NotificationPreferences reviewerPreferences = notificationPreferencesService.getUserNotificationPreferences(reviewerId);

                if (reviewerPreferences.isAllSOPAlertsEnabled() || reviewerPreferences.isReviewerAlertsEnabled()) {
                    CreateNotificationDto notificationDto = prepareNotificationDto(
                            reviewerId,
                            NotificationType.SOP_APPROVED,
                            "The SOP  " + sopDto.getTitle() + " you reviewed has been approved",
                            sopDto.getId(),
                            sopDto.getTitle()
                    );

                    createAndSendNotification(notificationDto);
                    emailService.sendSOPWorkflowEmail(
                            reviewerPreferences.getEmail(),
                            "Reviewer",
                            "Reviewer",
                            "SOP Approved",
                            sopDto.getTitle(),
                            sopDto.getId(),
                            "sop-approved-reviewer"
                    );
                }
            }

            // Create and send notification for the approver
            NotificationPreferences approverPreferences = notificationPreferencesService.getUserNotificationPreferences(approverId);
            if(approverPreferences.isAllSOPAlertsEnabled() || approverPreferences.isApproverAlertsEnabled()) {
                CreateNotificationDto approverNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_APPROVED,
                        "You have approved SOP: " + sopDto.getTitle(),
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(approverNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        approverPreferences.getEmail(),
                        "Approver",
                        "Approver",
                        "SOP Approved",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-approved-approver"
                );
            }

        } catch (Exception e) {
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }


    @KafkaListener(topics="sop-reviewal-ready")
    public void sopReviewalReadyListener(String data) {
        try {
            log.info("Received sop reviewal ready event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID approverId = sopDto.getApproverId();

            // Check approver's preferences before we create and send notification to the approver
            NotificationPreferences approverPreferences = notificationPreferencesService.getUserNotificationPreferences(approverId);
            if(approverPreferences.isAllSOPAlertsEnabled() || approverPreferences.isApproverAlertsEnabled()) {
                CreateNotificationDto approverNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_REVIEW_REQUIRED,
                        "An SOP titled " + sopDto.getTitle() + " you are assigned to approve have been made ready for reviews",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(approverNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        approverPreferences.getEmail(),
                        "Approver",
                        "Approver",
                        "Review Required",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-reviewal-ready"
                );
            }

            // Loop through reviewers,Check reviewer's preferences before we create and send notifications for each
            for (UUID reviewerId : sopDto.getReviewers()) {
                NotificationPreferences reviewerPreferences = notificationPreferencesService.getUserNotificationPreferences(reviewerId);

                if (reviewerPreferences.isAllSOPAlertsEnabled() || reviewerPreferences.isReviewerAlertsEnabled()) {
                    CreateNotificationDto notificationDto = prepareNotificationDto(
                            reviewerId,
                            NotificationType.SOP_REVIEW_REQUIRED,
                            "An SOP titled " + sopDto.getTitle() + " you are assigned to review have been made ready for reviews",
                            sopDto.getId(),
                            sopDto.getTitle()
                    );

                    createAndSendNotification(notificationDto);
                    emailService.sendSOPWorkflowEmail(
                            reviewerPreferences.getEmail(),
                            "Reviewer",
                            "Reviewer",
                            "Review Required",
                            sopDto.getTitle(),
                            sopDto.getId(),
                            "sop-reviewal-ready"
                    );
                }
            }

        } catch (Exception e) {
            log.error("Failed to create and send notification for sop: {}", data, e);
        }
    }


    @KafkaListener(topics = "sop-published")
    void sopPublishedListener(String data) {
        try {
            log.info("Received sop published event: {}", data);

            SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
            UUID authorId = sopDto.getAuthorId();
            UUID approverId = sopDto.getApproverId();

            // Check author's preferences before we create and send notification for the author
            NotificationPreferences authorPreferences = notificationPreferencesService.getUserNotificationPreferences(authorId);
            if (authorPreferences.isAllSOPAlertsEnabled() || authorPreferences.isAuthorAlertsEnabled()) {
                CreateNotificationDto authorNotificationDto = prepareNotificationDto(
                        authorId,
                        NotificationType.SOP_PUBLISHED,
                        "An SOP "+ sopDto.getTitle() + "you authored has been published",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(authorNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        authorPreferences.getEmail(),
                        "Author",
                        "Author",
                        "SOP Published",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-published-author"
                );
            }

            // Loop through reviewers,Check reviewer's preferences before we create and send notifications for each
            for (UUID reviewerId : sopDto.getReviewers()) {
                NotificationPreferences reviewerPreferences = notificationPreferencesService.getUserNotificationPreferences(reviewerId);
                if (reviewerPreferences.isAllSOPAlertsEnabled() || reviewerPreferences.isReviewerAlertsEnabled()) {
                    CreateNotificationDto notificationDto = prepareNotificationDto(
                            reviewerId,
                            NotificationType.SOP_PUBLISHED,
                            "An SOP titled " + sopDto.getTitle() + " you reviewed has been published",
                            sopDto.getId(),
                            sopDto.getTitle()
                    );

                    createAndSendNotification(notificationDto);
                    emailService.sendSOPWorkflowEmail(
                            reviewerPreferences.getEmail(),
                            "Reviewer",
                            "Reviewer",
                            "SOP Published",
                            sopDto.getTitle(),
                            sopDto.getId(),
                            "sop-published"
                    );
                }
            }

            // Create and send notification for the approver
            NotificationPreferences approverPreferences = notificationPreferencesService.getUserNotificationPreferences(approverId);
            if(approverPreferences.isAllSOPAlertsEnabled() || approverPreferences.isApproverAlertsEnabled()) {
                CreateNotificationDto approverNotificationDto = prepareNotificationDto(
                        approverId,
                        NotificationType.SOP_PUBLISHED,
                        "An SOP titled " + sopDto.getTitle() + " you approved has been published",
                        sopDto.getId(),
                        sopDto.getTitle()
                );

                createAndSendNotification(approverNotificationDto);
                emailService.sendSOPWorkflowEmail(
                        approverPreferences.getEmail(),
                        "Approver",
                        "Approver",
                        "SOP Published",
                        sopDto.getTitle(),
                        sopDto.getId(),
                        "sop-published"
                );
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