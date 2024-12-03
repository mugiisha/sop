package com.user_management_service.user_management_service;

import com.user_management_service.user_management_service.models.Department;
import com.user_management_service.user_management_service.models.User;
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
public class UserLoader {

    @Bean
    public CommandLineRunner loadUsers(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            UserRoleClientService userRoleClientService
    ) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) {
                // Define the users to be created
                String[][] users = {
                        {"admin@domain.com", "Admin", "ADMIN", "Administration"},
                        {"manager@domain.com", "Manager", "REVIEWER", "Operations"},
                        {"approver@domain.com", "Employee", "APPROVER", "Human Resources"},
                        {"hod@domain.com", "Employee", "HOD", "Human Resources"},
                        {"author@domain.com", "Employee", "AUTHOR", "Human Resources"},
                        {"staff@domain.com", "Employee", "STAFF", "Human Resources"},
                        {"iradukundakvn8@gmail.com", "Employee", "ADMIN", "Administration"}
                };

                for (String[] userData : users) {
                    String email = userData[0];
                    String name = userData[1];
                    String role = userData[2];
                    String departmentName = userData[3];

                    // Check if the user already exists
                    Optional<User> existingUser = userRepository.findByEmail(email);
                    if (existingUser.isPresent()) {
                        User user = existingUser.get();

                        // Check and update the user's role if needed
                        GetRoleByUserIdResponse userRole = userRoleClientService.getUserRoles(user.getId().toString());
                        if (!userRole.getSuccess() || !userRole.getRoleName().equals(role)) {
                            GetRoleByNameResponse roleResponse = userRoleClientService.getRoleByName(role);
                            userRoleClientService.assignRole(
                                    user.getId().toString(),
                                    roleResponse.getRoleId(),
                                    user.getDepartment().getId().toString());
                            System.out.println("Updated user " + email + " to role " + role);
                        } else {
                            System.out.println("User " + email + " already exists with role " + role + ". Skipping.");
                        }
                        continue;
                    }

                    // Check if the department exists or create it
                    Department department = departmentRepository.findByName(departmentName)
                            .orElseGet(() -> {
                                Department newDepartment = new Department();
                                newDepartment.setName(departmentName);
                                newDepartment.setDescription(departmentName + " Department");
                                newDepartment.setCreatedAt(LocalDateTime.now());
                                newDepartment.setUpdatedAt(LocalDateTime.now());
                                return departmentRepository.save(newDepartment);
                            });

                    // Create a new user
                    User newUser = new User();
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setPasswordHash(passwordEncoder.encode("defaultPassword123?")); // Temporary password
                    newUser.setDepartment(department);
                    newUser.setActive(true);
                    newUser.setEmailVerified(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    newUser.setFailedLoginAttempts(0);

                    User savedUser = userRepository.save(newUser);

                    // Assign the role to the user
                    GetRoleByNameResponse roleResponse = userRoleClientService.getRoleByName(role);
                    userRoleClientService.assignRole(
                            savedUser.getId().toString(),
                            roleResponse.getRoleId(),
                            department.getId().toString());

                    System.out.println("Created user " + email + " with role " + role);
                }
            }
        };
    }
}