package com.role_access_control_service.role_access_control_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RoleAssignment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private UUID userId;
    @ManyToOne
    @JoinColumn(name = "roleId")
    private Role role;
    @Column(nullable = false)
    private UUID departmentId;
    private LocalDateTime assignedAt;
}
