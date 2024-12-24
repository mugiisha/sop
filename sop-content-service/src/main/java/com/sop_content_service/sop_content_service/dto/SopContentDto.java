package com.sop_content_service.sop_content_service.dto;

import com.sop_content_service.sop_content_service.enums.SOPStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class SopContentDto {
    @NotEmpty(message = "SOP description is required")
    private String description;
    @NotEmpty(message = "SOP body is required")
    private String body;
    private SOPStatus status = SOPStatus.DRAFTED;
}
