package compliance_reporting_service.compliance_reporting_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ComplianceReportingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComplianceReportingServiceApplication.class, args);
	}

}
