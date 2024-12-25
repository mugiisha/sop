package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private final FeedbackRepository feedbackRepository;

    public ReportGenerator(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public byte[] generateExcelReport(List<FeedbackModel> feedbacks) throws IOException {
        // Create a workbook and sheet
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Feedback Report");

        // Create a font and set it as bold
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex()); // White text color for headers

        // Create a cell style for headers with background color
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex()); // Green background for header
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Fill background color

        // Header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("SOP Title");
        headerRow.createCell(1).setCellValue("UserName");
        headerRow.createCell(2).setCellValue("Role");
        headerRow.createCell(3).setCellValue("Department");
        headerRow.createCell(4).setCellValue("Content");
        headerRow.createCell(5).setCellValue("Response");
        headerRow.createCell(6).setCellValue("CreatedOn");

        // Apply header styles to the header row
        for (int i = 0; i < 7; i++) {
            headerRow.getCell(i).setCellStyle(headerCellStyle);
        }

        // Populate rows with feedback data
        int rowIdx = 1;
        for (FeedbackModel feedback : feedbacks) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(feedback.getTitle());
            row.createCell(1).setCellValue(feedback.getUserName());
            row.createCell(2).setCellValue(feedback.getRole());
            row.createCell(3).setCellValue(feedback.getDepartmentName());
            row.createCell(4).setCellValue(feedback.getContent());
            row.createCell(5).setCellValue(feedback.getResponse() != null ? feedback.getResponse() : "No response yet");
            row.createCell(6).setCellValue(feedback.getTimestamp().toString());
        }

        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generatePdfReport(List<FeedbackModel> feedbacks) throws IOException {
        logger.info("Starting to generate PDF report");

        // Create a new PDF document
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
        Document document = new Document(pdfDocument);

        // Add a title to the document
        Paragraph title = new Paragraph("Feedback Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setUnderline();  // Underline the title text
        document.add(title);

        // Add generation date
        String generationDate = "Report Generated On: " + new Date().toString();
        document.add(new Paragraph(generationDate).setFontSize(12).setTextAlignment(TextAlignment.RIGHT));

        // Add space before the table
        document.add(new Paragraph("\n"));

        // Create a table with 7 columns and set its width to 100% of the page
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4, 4, 8, 8, 4}));
        table.setWidth(UnitValue.createPercentValue(100));

        // Add headers to the table
        table.addCell(new Cell().add(new Paragraph("SOP Title")).setBackgroundColor(ColorConstants.GREEN));
        table.addCell(new Cell().add(new Paragraph("UserName")).setBackgroundColor(ColorConstants.GREEN));
        table.addCell(new Cell().add(new Paragraph("Role")).setBackgroundColor(ColorConstants.GREEN));
        table.addCell(new Cell().add(new Paragraph("Department")).setBackgroundColor(ColorConstants.GREEN));
        table.addCell(new Cell().add(new Paragraph("Content")).setBackgroundColor(ColorConstants.GREEN));
        table.addCell(new Cell().add(new Paragraph("Response")).setBackgroundColor(ColorConstants.GREEN));
        table.addCell(new Cell().add(new Paragraph("CreatedOn")).setBackgroundColor(ColorConstants.GREEN));

        // Add feedback data to the table
        for (FeedbackModel feedback : feedbacks) {
            logger.debug("Processing feedback: {}", feedback);

            table.addCell(new Cell().add(new Paragraph(feedback.getTitle())).setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(feedback.getUserName())).setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(feedback.getRole())).setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(feedback.getDepartmentName())).setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(feedback.getContent())).setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(feedback.getResponse() != null ? feedback.getResponse() : "No response yet")).setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(feedback.getTimestamp().toString())).setTextAlignment(TextAlignment.LEFT));

            // Log each piece of data
            logger.info("Title: {}", feedback.getTitle());
            logger.info("UserName: {}", feedback.getUserName());
            logger.info("Role: {}", feedback.getRole());
            logger.info("Department: {}", feedback.getDepartmentName());
            logger.info("Content: {}", feedback.getContent());
            logger.info("Response: {}", feedback.getResponse() != null ? feedback.getResponse() : "No response yet");
            logger.info("CreatedOn: {}", feedback.getTimestamp());
        }

        // Add the table to the document
        document.add(table);

        // Close the document
        document.close();

        logger.info("PDF report generation completed");

        return outputStream.toByteArray();
    }

    public List<FeedbackModel> fetchFeedbackData(Date startDate, Date endDate) {
        // Fetch feedbacks within the date range
        return feedbackRepository.findByTimestampBetween(startDate, endDate);
    }
}