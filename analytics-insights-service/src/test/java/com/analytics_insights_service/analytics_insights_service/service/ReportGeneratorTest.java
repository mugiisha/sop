package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.model.FeedbackModel;
import com.analytics_insights_service.analytics_insights_service.repository.FeedbackRepository;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportGeneratorTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private ReportGenerator reportGenerator;

    private List<FeedbackModel> mockFeedbacks;

    @BeforeEach
    void setUp() {
        // Initialize mock feedback data
        mockFeedbacks = new ArrayList<>();
        FeedbackModel feedback = new FeedbackModel();
        feedback.setTitle("Test SOP");
        feedback.setUserName("John Doe");
        feedback.setRole("Engineer");
        feedback.setDepartmentName("IT");
        feedback.setContent("Test content");
        feedback.setResponse("Test response");
        feedback.setTimestamp(new Date());
        mockFeedbacks.add(feedback);
    }

    @Test
    void generateExcelReport_ShouldCreateValidExcelFile() throws IOException {
        // When
        byte[] excelBytes = reportGenerator.generateExcelReport(mockFeedbacks);

        // Then
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            XSSFSheet sheet = workbook.getSheet("Feedback Report");
            assertNotNull(sheet);

            // Verify headers
            assertEquals("SOP Title", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("UserName", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Role", sheet.getRow(0).getCell(2).getStringCellValue());
            assertEquals("Department", sheet.getRow(0).getCell(3).getStringCellValue());
            assertEquals("Content", sheet.getRow(0).getCell(4).getStringCellValue());
            assertEquals("Response", sheet.getRow(0).getCell(5).getStringCellValue());
            assertEquals("CreatedOn", sheet.getRow(0).getCell(6).getStringCellValue());

            // Verify data
            assertEquals("Test SOP", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("John Doe", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Engineer", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals("IT", sheet.getRow(1).getCell(3).getStringCellValue());
            assertEquals("Test content", sheet.getRow(1).getCell(4).getStringCellValue());
            assertEquals("Test response", sheet.getRow(1).getCell(5).getStringCellValue());
        }
    }

    @Test
    void generatePdfReport_ShouldCreateValidPdfFile() throws IOException {
        // When
        byte[] pdfBytes = reportGenerator.generatePdfReport(mockFeedbacks);

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Note: Further PDF content verification could be added using a PDF parsing library
    }

    @Test
    void fetchFeedbackData_ShouldReturnDataWithinDateRange() {
        // Given
        Date startDate = new Date();
        Date endDate = new Date();
        when(feedbackRepository.findByTimestampBetween(startDate, endDate))
                .thenReturn(mockFeedbacks);

        // When
        List<FeedbackModel> result = reportGenerator.fetchFeedbackData(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(mockFeedbacks.size(), result.size());
        verify(feedbackRepository).findByTimestampBetween(startDate, endDate);
    }

    @Test
    void generateExcelReport_WithEmptyList_ShouldCreateValidExcelFile() throws IOException {
        // When
        byte[] excelBytes = reportGenerator.generateExcelReport(new ArrayList<>());

        // Then
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            XSSFSheet sheet = workbook.getSheet("Feedback Report");
            assertNotNull(sheet);
            // Should only have header row
            assertEquals(1, sheet.getPhysicalNumberOfRows());
        }
    }

    @Test
    void generatePdfReport_WithEmptyList_ShouldCreateValidPdfFile() throws IOException {
        // When
        byte[] pdfBytes = reportGenerator.generatePdfReport(new ArrayList<>());

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateExcelReport_WithNullResponse_ShouldHandleGracefully() throws IOException {
        // Given
        FeedbackModel feedbackWithNullResponse = new FeedbackModel();
        feedbackWithNullResponse.setTitle("Test SOP");
        feedbackWithNullResponse.setUserName("John Doe");
        feedbackWithNullResponse.setResponse(null);
        List<FeedbackModel> feedbacks = List.of(feedbackWithNullResponse);

        // When
        byte[] excelBytes = reportGenerator.generateExcelReport(feedbacks);

        // Then
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            XSSFSheet sheet = workbook.getSheet("Feedback Report");
            assertEquals("No response yet", sheet.getRow(1).getCell(5).getStringCellValue());
        }
    }

    @Test
    void fetchFeedbackData_WithNullDates_ShouldHandleGracefully() {
        // Given
        when(feedbackRepository.findByTimestampBetween(null, null))
                .thenReturn(new ArrayList<>());

        // When
        List<FeedbackModel> result = reportGenerator.fetchFeedbackData(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(feedbackRepository).findByTimestampBetween(null, null);
    }
}