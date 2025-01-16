// DepartmentRepository.java
package com.user_management_service.user_management_service.repositories;

import com.user_management_service.user_management_service.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT d FROM Department d WHERE d.active = true")
    List<Department> findAllByActiveTrue();
}