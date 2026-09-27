package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.clientDashboard.UserRiskDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating Excel reports for user risk analysis
 */
@Component
@Slf4j
public class ExcelGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generates an Excel file for user risk analysis
     * 
     * @param riskDetails List of user risk detail DTOs
     * @param fileName The name of the file to generate
     * @return Download URL for the generated Excel file
     */
    public String generateUserRiskAnalysisExcel(List<UserRiskDetailDto> riskDetails, String fileName) {
        log.info("Generating Excel file for user risk analysis: {}", fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("User Risk Analysis");

            // Create header row
            createHeaderRow(sheet, workbook);

            // Create data rows
            createDataRows(sheet, workbook, riskDetails);

            // Auto-size columns
            autoSizeColumns(sheet);

            // Generate file and return download URL
            return generateFileAndGetUrl(workbook, fileName);

        } catch (Exception e) {
            log.error("Error generating Excel file for user risk analysis: {}", fileName, e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    /**
     * Creates the header row for the Excel sheet
     */
    private void createHeaderRow(Sheet sheet, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Define headers
        String[] headers = {
            "User ID", "User Name", "User Email", "Risk Category", "Overall Progress (%)",
            "Total Sub-Packages", "Completed", "In Progress", "Not Started", "Exam Ready",
            "Last Activity Date", "Expired Sub-Packages", "Expiring Soon", 
            "Total Exam Attempts", "Passed Exams", "Failed Exams"
        };

        // Create header cells
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Creates data rows for the Excel sheet
     */
    private void createDataRows(Sheet sheet, Workbook workbook, List<UserRiskDetailDto> riskDetails) {
        CellStyle dataStyle = workbook.createCellStyle();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));

        int rowNum = 1;
        for (UserRiskDetailDto detail : riskDetails) {
            Row row = sheet.createRow(rowNum++);

            // User ID
            createCell(row, 0, detail.getUserId(), dataStyle);

            // User Name
            createCell(row, 1, detail.getUserName(), dataStyle);

            // User Email
            createCell(row, 2, detail.getUserEmail(), dataStyle);

            // Risk Category
            createCell(row, 3, detail.getRiskCategory(), dataStyle);

            // Overall Progress
            createCell(row, 4, detail.getOverallProgress() != null ? detail.getOverallProgress() : 0.0, dataStyle);

            // Total Sub-Packages
            createCell(row, 5, detail.getTotalSubPackages() != null ? detail.getTotalSubPackages() : 0, dataStyle);

            // Completed
            createCell(row, 6, detail.getCompletedSubPackages() != null ? detail.getCompletedSubPackages() : 0, dataStyle);

            // In Progress
            createCell(row, 7, detail.getInProgressSubPackages() != null ? detail.getInProgressSubPackages() : 0, dataStyle);

            // Not Started
            createCell(row, 8, detail.getNotStartedSubPackages() != null ? detail.getNotStartedSubPackages() : 0, dataStyle);

            // Exam Ready
            createCell(row, 9, detail.getExamReadySubPackages() != null ? detail.getExamReadySubPackages() : 0, dataStyle);

            // Last Activity Date
            if (detail.getLastActivityDate() != null) {
                Cell dateCell = row.createCell(10);
                dateCell.setCellValue(detail.getLastActivityDate().toString());
                dateCell.setCellStyle(dateStyle);
            } else {
                createCell(row, 10, "N/A", dataStyle);
            }

            // Expired Sub-Packages
            createCell(row, 11, detail.getExpiredSubPackages() != null ? String.join(", ", detail.getExpiredSubPackages()) : "", dataStyle);

            // Expiring Soon
            createCell(row, 12, detail.getExpiringSoonSubPackages() != null ? String.join(", ", detail.getExpiringSoonSubPackages()) : "", dataStyle);

            // Total Exam Attempts
            createCell(row, 13, detail.getTotalExamAttempts() != null ? detail.getTotalExamAttempts() : 0, dataStyle);

            // Passed Exams
            createCell(row, 14, detail.getPassedExams() != null ? detail.getPassedExams() : 0, dataStyle);

            // Failed Exams
            createCell(row, 15, detail.getFailedExams() != null ? detail.getFailedExams() : 0, dataStyle);
        }
    }

    /**
     * Creates a cell with the specified value and style
     */
    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        
        cell.setCellStyle(style);
    }

    /**
     * Auto-sizes all columns in the sheet
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 16; i++) { // 16 columns total
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Generates the file and returns a download URL
     * This is a placeholder implementation - in a real scenario, you would:
     * 1. Save the file to cloud storage (Azure Blob, AWS S3, etc.)
     * 2. Return the public download URL
     */
    private String generateFileAndGetUrl(Workbook workbook, String fileName) throws IOException {
        // Convert workbook to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        byte[] fileBytes = outputStream.toByteArray();

        // In a real implementation, you would upload to cloud storage here
        // For now, return a placeholder URL
        log.info("Excel file generated successfully: {} ({} bytes)", fileName, fileBytes.length);
        
        // Placeholder URL - replace with actual cloud storage URL
        return "https://your-storage-account.blob.core.windows.net/reports/" + fileName;
    }
}