package compliance_reporting_service.compliance_reporting_service.model;


import jdk.jfr.DataAmount;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = "sop_reports")
public class SOPReport {
    @Id
    private String id;
    private String SopId;


    private String title;
    private String initiatedBy;
    private int numberOfVersions;
    private String visibility;
    private int reads;
    private LocalDate createdAt;
    private LocalDate updateAt;

    // Derived field for reports
    private boolean completedOnTime;
}
