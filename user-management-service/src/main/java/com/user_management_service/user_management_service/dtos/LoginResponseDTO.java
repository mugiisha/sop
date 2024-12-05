package com.user_management_service.user_management_service.dtos;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String role;
    private String token;
    private UserDTO user;
}