package compliance_reporting_service.compliance_reporting_service.model;


import jdk.jfr.DataAmount;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Data
@Document(collection = "sop_reports")
public class SOPReport {
    @Id
    private String id;
    private String SopId;
    private String title;
    private String initiatedBy;
    private String visibility;
    private int reads;
    private Date createdAt;
    private Date updateAt;


}
