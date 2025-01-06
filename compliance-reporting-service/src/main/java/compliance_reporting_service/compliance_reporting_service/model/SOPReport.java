package compliance_reporting_service.compliance_reporting_service.model;


import jdk.jfr.DataAmount;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "sop_reports")
public class SOPReport {
    @Id
    private String id;
    private String SopId;
    private String title;
    private String visibility;
    private int reads;
    private Date createdAt;
    private Date updatedAt;


    private UUID authorId;
    private List<UUID> reviewers;
    private UUID approverId;



}
