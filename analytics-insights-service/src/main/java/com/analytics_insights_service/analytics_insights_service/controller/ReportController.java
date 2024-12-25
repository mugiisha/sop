package com.analytics_insights_service.analytics_insights_service.controller;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.service.ReportGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback/reports")
public class ReportController {

    private final ReportGenerator reportGenerator;

    public ReportController(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) throws IOException {
        List<FeedbackModel> feedbacks = reportGenerator.fetchFeedbackData(startDate, endDate);
        byte[] pdfBytes = reportGenerator.generatePdfReport(feedbacks);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=FeedbackReport.pdf")
                .body(pdfBytes);
    }

    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> generateExcelReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) throws IOException {
        List<FeedbackModel> feedbacks = reportGenerator.fetchFeedbackData(startDate, endDate);
        byte[] excelBytes = reportGenerator.generateExcelReport(feedbacks);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=FeedbackReport.xlsx")
                .body(excelBytes);
    }

}

