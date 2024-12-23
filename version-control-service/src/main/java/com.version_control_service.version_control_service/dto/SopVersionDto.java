package com.version_control_service.version_control_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SopVersionDto {
    Float versionNumber;
    Boolean currentVersion;
}
