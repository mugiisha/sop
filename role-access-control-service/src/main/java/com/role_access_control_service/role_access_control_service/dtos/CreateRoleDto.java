package com.role_access_control_service.role_access_control_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRoleDto {

    @NotBlank(message = "Role name is required")
    String roleName;
}
