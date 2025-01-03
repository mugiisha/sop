package com.version_control_service.version_control_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SopVersionDto {
    Float versionNumber;
    Boolean currentVersion;
}
