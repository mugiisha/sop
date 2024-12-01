// UserRepository.java
package com.user_management_service.user_management_service.repositories;

import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    // Basic user management queries
    List<User> findByDepartment(Department department);
    List<User> findByDepartmentId(UUID departmentId);
    List<User> findByEmailVerifiedFalse();

    // Activity related queries
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findByLastLoginBefore(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :date")
    List<User> findByLastLoginBeforeOrLastLoginIsNull(@Param("date") LocalDateTime date);

    // Department related queries
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.active = true")
    List<User> findActiveUsersByDepartment(@Param("departmentId") UUID departmentId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    long countByDepartment(@Param("departmentId") UUID departmentId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId AND u.active = true")
    long countActiveUsersByDepartment(@Param("departmentId") UUID departmentId);

    // Analytics queries
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :date")
    List<User> findUnverifiedUsersBefore(@Param("date") LocalDateTime date);
    // In UserRepository.java
    Optional<User> findByEmailVerificationToken(String token);

    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countVerifiedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    // Admin operations
    @Query("SELECT u FROM User u WHERE u.active = false AND u.deactivatedAt < :date")
    List<User> findDeactivatedUsersBefore(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u " +
            "WHERE u.department.id = :departmentId " +
            "AND u.active = true " +
            "AND u.emailVerified = true " +
            "AND u.lastLogin > :lastLoginDate")
    List<User> findActiveDepartmentUsersWithRecentLogin(
            @Param("departmentId") UUID departmentId,
            @Param("lastLoginDate") LocalDateTime lastLoginDate);

    // Batch operations
    @Query("UPDATE User u SET u.active = false " +
            "WHERE u.lastLogin < :inactiveDate " +
            "AND u.active = true")
    int deactivateInactiveUsers(@Param("inactiveDate") LocalDateTime inactiveDate);
}