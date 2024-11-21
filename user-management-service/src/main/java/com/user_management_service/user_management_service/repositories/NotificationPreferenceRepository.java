package com.user_management_service.user_management_service.repositories;

import com.user_management_service.user_management_service.models.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
    Optional<NotificationPreference> findByUserId(UUID userId);

    List<NotificationPreference> findByEmailEnabled(boolean emailEnabled);

    @Query("SELECT np FROM NotificationPreference np WHERE np.user.department.id = :departmentId")
    List<NotificationPreference> findByDepartment(@Param("departmentId") UUID departmentId);
}