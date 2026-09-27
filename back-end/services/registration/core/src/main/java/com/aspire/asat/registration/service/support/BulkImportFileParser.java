package com.aspire.asat.registration.service.support;

import com.aspire.asat.registration.data.endUser.request.EndUserRequestDTO;
import com.aspire.asat.registration.exception.CustomException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Shared CSV/Excel parser for end-user bulk import flows.
 * Legacy import filters incomplete rows; validate-and-review keeps them for admin correction.
 */
@Slf4j
@Component
public class BulkImportFileParser {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("csv", "xls", "xlsx");

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RegistationValidationException("Import file is empty");
        }
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RegistationValidationException("Only CSV and Excel files (.csv, .xls, .xlsx) are supported");
        }
    }

    /**
     * Parses file for the legacy import path: skips empty, malformed, and incomplete rows
     * (same behavior as the original EndUserServiceImpl parser).
     */
    public List<EndUserRequestDTO> parseForLegacyImport(MultipartFile file, String clientAdminId) {
        return parseRows(file).stream()
                .filter(this::isCompleteForLegacyImport)
                .map(row -> toRequestDto(row, clientAdminId))
                .toList();
    }

    /**
     * Parses file for validate-and-review: keeps incomplete/non-empty rows so they can be marked invalid.
     */
    public List<ParsedBulkImportRow> parseForValidation(MultipartFile file) {
        return parseRows(file);
    }

    private List<ParsedBulkImportRow> parseRows(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        if ("csv".equals(extension)) {
            return parseCsv(file);
        }
        return parseExcel(file);
    }

    private List<ParsedBulkImportRow> parseCsv(MultipartFile file) {
        List<ParsedBulkImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                rowIndex++;
                String[] fields = line.split(",", -1);
                if (isEmptyCsvRow(fields)) {
                    continue;
                }
                if (fields.length < 4) {
                    // Keep partial rows for validation flow; legacy filters them out via isCompleteForLegacyImport
                    rows.add(ParsedBulkImportRow.builder()
                            .rowIndex(rowIndex)
                            .firstName(safeField(fields, 0))
                            .lastName(safeField(fields, 1))
                            .email(safeField(fields, 2))
                            .phoneNumber(safeField(fields, 3))
                            .department("")
                            .countryCode("")
                            .complete(false)
                            .build());
                    continue;
                }
                rows.add(buildRow(rowIndex, fields));
            }
        } catch (IOException e) {
            log.error("CSV file parsing failed", e);
            throw new CustomException("CSV parsing failed", e);
        }
        return rows;
    }

    private List<ParsedBulkImportRow> parseExcel(MultipartFile file) {
        List<ParsedBulkImportRow> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;
            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (row == null || isEmptyExcelRow(row, formatter)) {
                    continue;
                }
                int rowIndex = row.getRowNum(); // 1-based data rows when header is row 0
                String[] fields = new String[6];
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = getCellValueAsString(row.getCell(i), formatter);
                }
                if (isInvalidColumnCount(fields)) {
                    rows.add(ParsedBulkImportRow.builder()
                            .rowIndex(rowIndex)
                            .firstName(fields[0])
                            .lastName(fields[1])
                            .email(fields[2])
                            .phoneNumber(fields[3])
                            .department(fields[4])
                            .countryCode(fields[5])
                            .complete(false)
                            .build());
                    continue;
                }
                rows.add(buildRow(rowIndex, fields));
            }
        } catch (IOException e) {
            log.error("Excel file parsing failed", e);
            throw new CustomException("Excel parsing failed", e);
        } catch (Exception e) {
            log.error("Excel file parsing failed", e);
            throw new RegistationValidationException("Invalid Excel file format");
        }
        return rows;
    }

    private ParsedBulkImportRow buildRow(int rowIndex, String[] fields) {
        String firstName = safeField(fields, 0);
        String lastName = safeField(fields, 1);
        String email = safeField(fields, 2);
        String phoneNumber = safeField(fields, 3);
        String department = safeField(fields, 4);
        String countryCode = safeField(fields, 5);
        boolean complete = !firstName.isEmpty() && !email.isEmpty() && !phoneNumber.isEmpty() && fields.length >= 4;
        return ParsedBulkImportRow.builder()
                .rowIndex(rowIndex)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phoneNumber)
                .department(department)
                .countryCode(countryCode)
                .complete(complete)
                .build();
    }

    private boolean isCompleteForLegacyImport(ParsedBulkImportRow row) {
        return row.isComplete();
    }

    private EndUserRequestDTO toRequestDto(ParsedBulkImportRow row, String clientAdminId) {
        EndUserRequestDTO dto = new EndUserRequestDTO();
        dto.setFirstName(row.getFirstName());
        dto.setLastName(row.getLastName() != null ? row.getLastName() : "");
        dto.setEmail(row.getEmail());
        dto.setPhoneNumber(row.getPhoneNumber());
        dto.setDepartment(row.getDepartment() != null ? row.getDepartment() : "");
        dto.setCountryCode(row.getCountryCode() != null ? row.getCountryCode() : "");
        dto.setClientAdminId(clientAdminId);
        return dto;
    }

    private static String safeField(String[] fields, int index) {
        if (fields == null || index >= fields.length || fields[index] == null) {
            return "";
        }
        return fields[index].trim();
    }

    private static boolean isEmptyCsvRow(String[] fields) {
        if (fields == null || fields.length == 0) {
            return true;
        }
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInvalidColumnCount(String[] fields) {
        return fields.length < 4;
    }

    private static String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static boolean isEmptyExcelRow(Row row, DataFormatter formatter) {
        for (int i = 0; i < 6; i++) {
            if (!getCellValueAsString(row.getCell(i), formatter).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    @Data
    @Builder
    public static class ParsedBulkImportRow {
        private int rowIndex;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String department;
        private String countryCode;
        private boolean complete;
    }
}
