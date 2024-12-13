package com.notification_service.notification_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomUserDto {
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String verificationToken;
}
