package compliance_reporting_service.compliance_reporting_service.controller;

import compliance_reporting_service.compliance_reporting_service.dto.ReportResponseDto;
import compliance_reporting_service.compliance_reporting_service.model.SOPReport;
import compliance_reporting_service.compliance_reporting_service.service.SOPReportService;
import compliance_reporting_service.compliance_reporting_service.util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class SOPReportController {

    private final SOPReportService reportService;


    @GetMapping
    public Response<List<ReportResponseDto>> getAllReports() {
        List<ReportResponseDto> report = reportService.findAll();
        return new Response<List<ReportResponseDto>>(true,"Report fetched successfully", report);
    }

}




