package compliance_reporting_service.compliance_reporting_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private String reportId;
    private String title;
    private int numberOfVersions;
    private Date createdAt;    // Changed from LocalDateTime to Date
    private Date updatedAt;    // Changed from LocalDateTime to Date
    private int reads;
    private String visibility;
    private String author;
    private String approver;
    private List<String> reviewers;
}