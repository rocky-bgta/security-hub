package com.aspire.asat.registration.service.support;

import com.aspire.asat.registration.data.endUser.request.EndUserRequestDTO;
import com.aspire.asat.registration.exception.RegistationValidationException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BulkImportFileParserTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String CSV_HEADER = "firstName,lastName,email,phoneNumber,department,countryCode\n";

    private BulkImportFileParser parser;

    @BeforeEach
    void setUp() {
        parser = new BulkImportFileParser();
    }

    @Test
    void validateFile_Empty_Throws() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        RegistationValidationException ex = assertThrows(
                RegistationValidationException.class,
                () -> parser.validateFile(file));
        assertEquals("Import file is empty", ex.getMessage());
    }

    @Test
    void validateFile_UnsupportedExtension_Throws() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("users.pdf");

        RegistationValidationException ex = assertThrows(
                RegistationValidationException.class,
                () -> parser.validateFile(file));
        assertEquals("Only CSV and Excel files (.csv, .xls, .xlsx) are supported", ex.getMessage());
    }

    @Test
    void parseForValidation_Csv_KeepsIncompleteRows() throws IOException {
        String csv = CSV_HEADER
                + "John,Doe,john@company.com,123,IT,US\n"
                + ",Doe,missing.first@company.com,123,IT,US\n"
                + "\n"
                + "OnlyName\n";

        List<BulkImportFileParser.ParsedBulkImportRow> rows =
                parser.parseForValidation(csvFile(csv, "users.csv"));

        assertEquals(3, rows.size());
        assertTrue(rows.get(0).isComplete());
        assertEquals("John", rows.get(0).getFirstName());
        assertFalse(rows.get(1).isComplete());
        assertFalse(rows.get(2).isComplete());
        assertEquals("OnlyName", rows.get(2).getFirstName());
    }

    @Test
    void parseForLegacyImport_Csv_SkipsIncompleteRows() throws IOException {
        String csv = CSV_HEADER
                + "John,Doe,john@company.com,123,IT,US\n"
                + ",Doe,missing.first@company.com,123,IT,US\n"
                + "OnlyName\n";

        List<EndUserRequestDTO> rows =
                parser.parseForLegacyImport(csvFile(csv, "users.csv"), CLIENT_ADMIN_ID);

        assertEquals(1, rows.size());
        assertEquals("john@company.com", rows.get(0).getEmail());
        assertEquals(CLIENT_ADMIN_ID, rows.get(0).getClientAdminId());
        assertEquals("IT", rows.get(0).getDepartment());
        assertEquals("US", rows.get(0).getCountryCode());
    }

    @Test
    void parseForValidation_UppercaseCsvExtension_Works() throws IOException {
        String csv = CSV_HEADER + "Jane,Smith,jane@company.com,555,HR,BD\n";
        List<BulkImportFileParser.ParsedBulkImportRow> rows =
                parser.parseForValidation(csvFile(csv, "USERS.CSV"));

        assertEquals(1, rows.size());
        assertEquals("Jane", rows.get(0).getFirstName());
        assertEquals("BD", rows.get(0).getCountryCode());
    }

    @Test
    void parseForValidation_Excel_ParsesRows() throws IOException {
        MultipartFile file = excelFile(
                new String[]{"firstName", "lastName", "email", "phoneNumber", "department", "countryCode"},
                new String[]{"Alice", "Brown", "alice@company.com", "111", "Eng", "US"},
                new String[]{"", "Brown", "incomplete@company.com", "222", "Eng", "US"}
        );

        List<BulkImportFileParser.ParsedBulkImportRow> rows = parser.parseForValidation(file);

        assertEquals(2, rows.size());
        assertTrue(rows.get(0).isComplete());
        assertEquals("alice@company.com", rows.get(0).getEmail());
        assertFalse(rows.get(1).isComplete());
    }

    @Test
    void parseForLegacyImport_Excel_SkipsIncompleteRows() throws IOException {
        MultipartFile file = excelFile(
                new String[]{"firstName", "lastName", "email", "phoneNumber", "department", "countryCode"},
                new String[]{"Alice", "Brown", "alice@company.com", "111", "Eng", "US"},
                new String[]{"", "Brown", "incomplete@company.com", "222", "Eng", "US"}
        );

        List<EndUserRequestDTO> rows = parser.parseForLegacyImport(file, CLIENT_ADMIN_ID);

        assertEquals(1, rows.size());
        assertEquals("Alice", rows.get(0).getFirstName());
        assertEquals(CLIENT_ADMIN_ID, rows.get(0).getClientAdminId());
    }

    @Test
    void getFileExtension_EdgeCases() {
        assertEquals("", BulkImportFileParser.getFileExtension(null));
        assertEquals("", BulkImportFileParser.getFileExtension(""));
        assertEquals("", BulkImportFileParser.getFileExtension("noext"));
        assertEquals("csv", BulkImportFileParser.getFileExtension("a.CSV"));
    }

    private MultipartFile csvFile(String content, String filename) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        return file;
    }

    private MultipartFile excelFile(String[] header, String[]... dataRows) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                headerRow.createCell(i).setCellValue(header[i]);
            }
            for (int r = 0; r < dataRows.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < dataRows[r].length; c++) {
                    row.createCell(c).setCellValue(dataRows[r][c]);
                }
            }
            workbook.write(out);
        }
        byte[] bytes = out.toByteArray();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("users.xlsx");
        when(file.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(bytes));
        return file;
    }
}
