package com.user_management_service.user_management_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/* this stores the data to be transferred between the user-management-service
 and the notification-service */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDto {
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String verificationToken;

}
