package com.version_control_service.version_control_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SopVersionDto {
    Float versionNumber;
    Boolean currentVersion;
    private Date createdAt;
    private Date updatedAt;
}
