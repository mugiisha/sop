package compliance_reporting_service.compliance_reporting_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SopVersionDto {
    Float versionNumber;
    boolean currentVersion;
}
