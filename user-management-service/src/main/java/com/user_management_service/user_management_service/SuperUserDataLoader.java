package com.user_management_service.user_management_service;

import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.enums.Role;
import com.user_management_service.user_management_service.repositories.DepartmentRepository;
import com.user_management_service.user_management_service.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class SuperUserDataLoader {

    @Bean
    public CommandLineRunner loadSuperUser(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) {
                String superUserEmail = "ndayambaje.virgile@amalitechtraining.org";

                // Check if superuser already exists
                Optional<User> existingUser = userRepository.findByEmail(superUserEmail);
                if (existingUser.isPresent()) {
                    User user = existingUser.get();
                    // Ensure the existing user has ADMIN role
                    if (user.getRole() != Role.ADMIN) {
                        user.setRole(Role.ADMIN);
                        userRepository.save(user);
                        System.out.println("Updated existing superuser role to ADMIN.");
                    } else {
                        System.out.println("Superuser already exists with ADMIN role. Skipping creation.");
                    }
                    return;
                }

                // Check if admin department exists or create it
                Department adminDept = departmentRepository.findByName("Administration")
                        .orElseGet(() -> {
                            Department dept = new Department();
                            dept.setName("Administration");
                            dept.setDescription("System Administration Department");
                            dept.setCreatedAt(LocalDateTime.now());
                            dept.setUpdatedAt(LocalDateTime.now());
                            return departmentRepository.save(dept);
                        });

                // Create superuser
                User superUser = new User();
                superUser.setName("Admin");
                superUser.setEmail(superUserEmail);
                superUser.setPasswordHash(passwordEncoder.encode("changeMe123?")); // Should be changed on first login
                superUser.setDepartment(adminDept);
                superUser.setActive(true);
                superUser.setEmailVerified(true);
                superUser.setCreatedAt(LocalDateTime.now());
                superUser.setUpdatedAt(LocalDateTime.now());
                superUser.setFailedLoginAttempts(0);
                superUser.setRole(Role.ADMIN); // Set the role to ADMIN

                User savedUser = userRepository.save(superUser);
                System.out.println("Created new superuser with ADMIN role: " + savedUser.getEmail());
            }
        };
    }
}