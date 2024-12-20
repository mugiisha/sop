package compliance_reporting_service.compliance_reporting_service.controller;

import compliance_reporting_service.compliance_reporting_service.model.SOPReport;
import compliance_reporting_service.compliance_reporting_service.service.SOPReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class SOPReportController {

    private final SOPReportService reportService;



}




