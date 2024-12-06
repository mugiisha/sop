package com.notification_service.notification_service.dtos;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SOPDto {
    private UUID id;
    private String title;
    private List<UUID> reviewers;
    private List<UUID> approvers;

}
