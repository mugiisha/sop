package com.notification_service.notification_service.repositories;

import com.notification_service.notification_service.models.NotificationPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationPreferencesRepository extends MongoRepository<NotificationPreferences,String> {
    NotificationPreferences findByEmail(String email);
    NotificationPreferences findByUserId(UUID userId);
}
