package compliance_reporting_service.compliance_reporting_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReportResponseDto {

    private String title;
    private int numberOfVersions;
    private String visibility;
    private int reads;
    private Date createdAt;
    private Date updateAt;
}
