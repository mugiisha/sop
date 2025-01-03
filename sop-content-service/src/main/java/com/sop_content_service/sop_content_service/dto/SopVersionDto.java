package com.sop_content_service.sop_content_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SopVersionDto {
    Float versionNumber;
    boolean currentVersion;
}
