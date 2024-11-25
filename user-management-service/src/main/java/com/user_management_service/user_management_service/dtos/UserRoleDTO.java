package com.user_management_service.user_management_service.dtos;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRoleDTO {
    private String id;
    private String email;
    private String name;
    private String role;
    private boolean isActive;
    private LocalDate dateAdded;


}

