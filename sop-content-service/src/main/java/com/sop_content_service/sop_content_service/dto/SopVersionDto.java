package com.sop_content_service.sop_content_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SopVersionDto {
    Float versionNumber;
    boolean currentVersion;
}
