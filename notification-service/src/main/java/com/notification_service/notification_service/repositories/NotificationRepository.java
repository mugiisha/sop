package com.notification_service.notification_service.repositories;

import com.notification_service.notification_service.models.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<Notification,String> {
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
