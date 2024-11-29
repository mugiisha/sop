package com.user_management_service.user_management_service;

import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.enums.Role;
import com.user_management_service.user_management_service.repositories.DepartmentRepository;
import com.user_management_service.user_management_service.repositories.UserRepository;
import com.user_management_service.user_management_service.services.UserRoleClientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import sopService.GetRoleByNameResponse;
import sopService.GetRoleByUserIdResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class SuperUserDataLoader {

    @Bean
    public CommandLineRunner loadSuperUser(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            UserRoleClientService userRoleClientService
    ) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) {
                String superUserEmail = "ndayambajevg16@gmail.com";

                // Check if superuser already exists
                Optional<User> existingUser = userRepository.findByEmail(superUserEmail);
                if (existingUser.isPresent()) {
                    User user = existingUser.get();

                    // get users role
                    GetRoleByUserIdResponse userRole = userRoleClientService.getUserRoles(user.getId().toString());
                    // Ensure the existing user has ADMIN role
                    if (!userRole.getSuccess() || !userRole.getRoleName().equals("ADMIN")) {
                        // assign the Admin role to the user
                        userRoleClientService.assignRole(
                                user.getId().toString(),
                                userRole.getRoleId(),
                                user.getDepartment().getId().toString());

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

                User savedUser = userRepository.save(superUser);

                // get Admin role by it's name
                GetRoleByNameResponse response = userRoleClientService.getRoleByName("ADMIN");

                // Assign the role to the user
                userRoleClientService.assignRole(
                        savedUser.getId().toString(),
                        response.getRoleId(),
                        adminDept.getId().toString());

                System.out.println("Created new superuser with ADMIN role: " + savedUser.getEmail());
            }
        };
    }
}