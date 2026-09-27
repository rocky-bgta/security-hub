package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentExportDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentRowDto;
import com.aspire.asat.cms.service.LicenseAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LicenseAssignmentControllerImplTest {

    @Mock
    private LicenseAssignmentService licenseAssignmentService;

    @InjectMocks
    private LicenseAssignmentControllerImpl controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listLicenseAssignments_shouldReturn200WithPayload() {
        AllResponseDto<List<LicenseAssignmentRowDto>> page = new AllResponseDto<>(0, 10, 1L, List.of(
                LicenseAssignmentRowDto.builder().fullName("John Smith").status("Active").build()));
        when(licenseAssignmentService.listLicenseAssignments(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<LicenseAssignmentRowDto>>>> response =
                controller.listLicenseAssignments(
                        null, null, null, null, null, null, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals(1L, response.getBody().getData().getTotal());
        assertEquals("John Smith", response.getBody().getData().getItems().get(0).getFullName());
    }

    @Test
    void listLicenseAssignments_shouldParseFlexibleDatesAndPassFilters() {
        when(licenseAssignmentService.listLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new AllResponseDto<>(0, 10, 0L, List.of()));

        controller.listLicenseAssignments(
                "client-1", null, "john", "pkg-gold", "prod-sat", "Finance", "ACTIVE",
                "2026/01/01", "2026-01-31", 0, 10);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(licenseAssignmentService).listLicenseAssignments(
                eq("client-1"), isNull(), eq("john"), eq("pkg-gold"), eq("prod-sat"), eq("Finance"), eq("ACTIVE"),
                fromCaptor.capture(), toCaptor.capture(), eq(0), eq(10));
        assertEquals(LocalDate.of(2026, 1, 1), fromCaptor.getValue());
        assertEquals(LocalDate.of(2026, 1, 31), toCaptor.getValue());
    }

    @Test
    void listLicenseAssignments_shouldPassExpiringSoonStatusThrough() {
        when(licenseAssignmentService.listLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new AllResponseDto<>(0, 10, 0L, List.of()));

        controller.listLicenseAssignments(
                null, null, null, null, null, "Audit/Internal Controls", "EXPIRING_SOON",
                null, null, 0, 10);

        verify(licenseAssignmentService).listLicenseAssignments(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("Audit/Internal Controls"), eq("EXPIRING_SOON"),
                isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    void listLicenseAssignments_shouldReturn400WhenToDateBeforeFromDate() {
        ResponseEntity<ApiResponseDto<AllResponseDto<List<LicenseAssignmentRowDto>>>> response =
                controller.listLicenseAssignments(
                        null, null, null, null, null, null, null, "2026-03-01", "2026-02-01", 0, 10);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatusCode());
        assertNull(response.getBody().getData());
    }

    @Test
    void exportLicenseAssignments_shouldReturnCsvAttachment() {
        when(licenseAssignmentService.exportLicenseAssignments(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq("csv")))
                .thenReturn(LicenseAssignmentExportDto.builder()
                        .content("User Name,Email\n".getBytes())
                        .filename("license-assignments.csv")
                        .contentType("text/csv; charset=UTF-8")
                        .build());

        ResponseEntity<byte[]> response = controller.exportLicenseAssignments(
                null, null, null, null, null, null, null, null, null, "csv");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE).startsWith("text/csv"));
        assertEquals("attachment; filename=\"license-assignments.csv\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getBody());
    }

    @Test
    void exportLicenseAssignments_shouldPassXlsFormatAndFilters() {
        when(licenseAssignmentService.exportLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(LicenseAssignmentExportDto.builder()
                        .content(new byte[] {1, 2, 3})
                        .filename("license-assignments.xls")
                        .contentType("application/vnd.ms-excel")
                        .build());

        controller.exportLicenseAssignments(
                "client-1", null, "john", "pkg-gold", "prod-sat", "Finance", "ACTIVE",
                "2026-01-01", "2026-01-31", "xls");

        verify(licenseAssignmentService).exportLicenseAssignments(
                eq("client-1"), isNull(), eq("john"), eq("pkg-gold"), eq("prod-sat"), eq("Finance"), eq("ACTIVE"),
                eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31)), eq("xls"));
    }

    @Test
    void exportLicenseAssignments_shouldReturn400WhenFormatInvalid() {
        when(licenseAssignmentService.exportLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Unsupported export format: pdf"));

        ResponseEntity<byte[]> response = controller.exportLicenseAssignments(
                null, null, null, null, null, null, null, null, null, "pdf");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
