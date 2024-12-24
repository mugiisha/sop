package compliance_reporting_service.compliance_reporting_service.repository;

import compliance_reporting_service.compliance_reporting_service.model.SOPReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface SOPReportRepository extends MongoRepository<SOPReport, String> {
    List<SOPReport> findByVisibility(String visibility);
//    List<SOPReport> findByCompletionDateBetween(LocalDate startDate, LocalDate endDate);
}


