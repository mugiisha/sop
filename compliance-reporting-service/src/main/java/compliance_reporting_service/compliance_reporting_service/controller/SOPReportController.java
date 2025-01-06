package compliance_reporting_service.compliance_reporting_service.controller;

import compliance_reporting_service.compliance_reporting_service.dto.ReportResponseDto;
import compliance_reporting_service.compliance_reporting_service.service.SOPReportService;
import compliance_reporting_service.compliance_reporting_service.util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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




