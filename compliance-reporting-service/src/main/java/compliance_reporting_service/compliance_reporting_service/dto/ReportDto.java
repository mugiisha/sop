package compliance_reporting_service.compliance_reporting_service.dto;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ReportDto {

    private String id;
    private String title;
    private String visibility;

    private Date createdAt;
    private Date updatedAt;

    private String authorId;
    private List<String> reviewerIds;
    private String approverId;
}
