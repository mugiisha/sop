package com.user_management_service.user_management_service.dtos;

import lombok.Data;

@Data
public class UserDetailsDTO {
    private String id;
    private String email;
    private String name;
    private String lastLoginAt;
}