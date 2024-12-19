package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.colors.ColorConstants;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.IndexedColors;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class ReportGenerator {

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
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("SOP ID");
        headerRow.createCell(2).setCellValue("User ID");
        headerRow.createCell(3).setCellValue("Content");
        headerRow.createCell(4).setCellValue("Response");
        headerRow.createCell(5).setCellValue("Timestamp");

        // Apply header styles to the header row
        for (int i = 0; i < 6; i++) {
            headerRow.getCell(i).setCellStyle(headerCellStyle);
        }

        // Populate rows with feedback data
        int rowIdx = 1;
        for (FeedbackModel feedback : feedbacks) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(feedback.getId());
            row.createCell(1).setCellValue(feedback.getSopId());
            row.createCell(2).setCellValue(feedback.getUserId());
            row.createCell(3).setCellValue(feedback.getContent());
            row.createCell(4).setCellValue(feedback.getResponse());
            row.createCell(5).setCellValue(feedback.getTimestamp().toString());
        }

        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }


    public byte[] generatePdfReport(List<FeedbackModel> feedbacks) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Initialize PDF writer
        PdfWriter writer = new PdfWriter(outputStream);

        // Initialize PDF document
        PdfDocument pdfDocument = new PdfDocument(writer);

        // Initialize layout document (correct usage of Document)
        Document document = new Document(pdfDocument);

        // Add title (green, underlined, and bold)
        document.add(new Paragraph("Feedback Report")
                .setBold()
                .setFontColor(ColorConstants.GREEN)
                .setUnderline()
                .setFontSize(18));

        // Add space between title and table
        document.add(new Paragraph("\n"));

        // Add generation date
        String generationDate = "Report Generated On: " + new Date().toString();
        document.add(new Paragraph(generationDate).setFontSize(12).setTextAlignment(TextAlignment.RIGHT));

        // Add space before the table
        document.add(new Paragraph("\n"));

        // Create the table with 5 columns
        Table table = new Table(5); // Number of columns

        // Add bold headers for each column
        table.addCell(new Cell().add(new Paragraph("ID").setBold()));
        table.addCell(new Cell().add(new Paragraph("SOP ID").setBold()));
        table.addCell(new Cell().add(new Paragraph("User ID").setBold()));
        table.addCell(new Cell().add(new Paragraph("Content").setBold()));
        table.addCell(new Cell().add(new Paragraph("Response").setBold()));

        // Add rows to the table
        for (FeedbackModel feedback : feedbacks) {
            table.addCell(feedback.getId());
            table.addCell(feedback.getSopId());
            table.addCell(feedback.getUserId());
            table.addCell(feedback.getContent());
            table.addCell(feedback.getResponse() != null ? feedback.getResponse() : "No response yet");
        }

        // Add table to the document
        document.add(table);

        // Close the document
        document.close();

        return outputStream.toByteArray();
    }


    public List<FeedbackModel> fetchFeedbackData(Date startDate, Date endDate) {
        // Fetch feedbacks within the date range
        return feedbackRepository.findByTimestampBetween(startDate, endDate);
    }

}
