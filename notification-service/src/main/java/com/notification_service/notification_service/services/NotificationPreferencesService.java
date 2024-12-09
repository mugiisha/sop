package com.notification_service.notification_service.services;

import com.notification_service.notification_service.dtos.UpdateNotificationPreferencesDto;
import com.notification_service.notification_service.models.NotificationPreferences;
import com.notification_service.notification_service.repositories.NotificationPreferencesRepository;
import com.notification_service.notification_service.utils.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class NotificationPreferencesService {

    private final NotificationPreferencesRepository notificationPreferenceRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationPreferencesService.class);
    private static final String NOT_FOUND_ERROR_MESSAGE = "Notification Preference not found by user id";

    @Autowired
    public NotificationPreferencesService(NotificationPreferencesRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    public void createNotificationPreferences(UUID userId, String email) {

        log.info("Creating user notification preferences");

        NotificationPreferences notificationPreference = new NotificationPreferences();
        notificationPreference.setUserId(userId);
        notificationPreference.setEmail(email);

        notificationPreferenceRepository.save(notificationPreference);
    }

    public NotificationPreferences updateNotificationPreferences(UUID userId, UpdateNotificationPreferencesDto updateNotificationPreference) {

        log.info("Updating user notification preferences");

        NotificationPreferences notificationPreference = notificationPreferenceRepository.findByUserId(userId);

        if(notificationPreference == null) {
            log.error(NOT_FOUND_ERROR_MESSAGE);
            throw new NotFoundException("Notification Preference not found for user id: " + userId);
        }

        notificationPreference.setAllSOPAlertsEnabled(updateNotificationPreference.isAllSOPAlertsEnabled());
        notificationPreference.setAuthorAlertsEnabled(updateNotificationPreference.isAuthorAlertsEnabled());
        notificationPreference.setReviewerAlertsEnabled(updateNotificationPreference.isReviewerAlertsEnabled());
        notificationPreference.setApproverAlertsEnabled(updateNotificationPreference.isApproverAlertsEnabled());

        return notificationPreferenceRepository.save(notificationPreference);
    }

    public NotificationPreferences getUserNotificationPreferences(UUID userId) {

        log.info("Getting user notification preferences");

        NotificationPreferences notificationPreference = notificationPreferenceRepository.findByUserId(userId);

        if(notificationPreference == null) {
            log.error(NOT_FOUND_ERROR_MESSAGE);
            throw new NotFoundException("Notification Preference not found for user id: "+userId);
        }

        return notificationPreference;
    }


    public void deleteNotificationPreferences(String userId) {

        log.info("Deleting user notification preferences");

        NotificationPreferences notificationPreference = notificationPreferenceRepository.findByEmail(userId);

        if(notificationPreference == null) {
            log.error(NOT_FOUND_ERROR_MESSAGE);
            throw new NotFoundException("Notification Preference not found for user id: "+userId);
        }

        notificationPreferenceRepository.delete(notificationPreference);
    }

    public boolean isAuthorAlertsEnabled(UUID authorId) {

            log.info("Checking if author alerts are enabled");

            NotificationPreferences notificationPreference = notificationPreferenceRepository.findByUserId(authorId);

            if(notificationPreference == null) {
                log.error("Notification Preference not found for user id: "+authorId);
                return true;
            }

            return notificationPreference.isAuthorAlertsEnabled();
    }

    public boolean isReviewerAlertsEnabled(UUID reviewerId) {

            log.info("Checking if reviewer alerts are enabled");

            NotificationPreferences notificationPreference = notificationPreferenceRepository.findByUserId(reviewerId);

            if(notificationPreference == null) {
                log.error("Notification Preference not found for user id: {}",reviewerId);
                return true;
            }

            return notificationPreference.isReviewerAlertsEnabled();
    }

    public boolean isApproverAlertsEnabled(UUID approverId) {

            log.info("Checking if approver alerts are enabled");

            NotificationPreferences notificationPreference = notificationPreferenceRepository.findByUserId(approverId);

            if(notificationPreference == null) {
                log.error("Notification Preference not found for user id: {}",approverId);
                return true;
            }

            return notificationPreference.isApproverAlertsEnabled();
    }

    public boolean isAllSOPAlertsEnabled(UUID userId) {

            log.info("Checking if all SOP alerts are enabled");

            NotificationPreferences notificationPreference = notificationPreferenceRepository.findByUserId(userId);

            if(notificationPreference == null) {
                log.error("Notification Preference not found for user id: {}", userId);
                return true;
            }

            return notificationPreference.isAllSOPAlertsEnabled();
    }
}
