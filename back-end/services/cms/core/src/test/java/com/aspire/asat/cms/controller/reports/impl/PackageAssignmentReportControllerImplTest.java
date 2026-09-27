package com.aspire.asat.cms.controller.reports.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;
import com.aspire.asat.cms.dto.reports.PackageAssignmentSummaryDTO;
import com.aspire.asat.cms.service.reports.PackageAssignmentReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PackageAssignmentReportControllerImplTest {

    @Mock
    private PackageAssignmentReportService packageAssignmentReportService;

    @InjectMocks
    private PackageAssignmentReportControllerImpl controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPackageAssignmentReport_shouldReturn200WithPayload() {
        PackageAssignmentReportDTO report = sampleReport();
        when(packageAssignmentReportService.getPackageAssignmentReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(report);

        ResponseEntity<ApiResponseDto<PackageAssignmentReportDTO>> response =
                controller.getPackageAssignmentReport(
                        null, null, null, null, null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals(10L, response.getBody().getData().getSummary().getTotalSubPackages());
        assertEquals(3L, response.getBody().getData().getSummary().getCompleteAssignments());
    }

    @Test
    void getPackageAssignmentReport_shouldParseFlexibleDatesAndPassCompleteFilter() {
        when(packageAssignmentReportService.getPackageAssignmentReport(
                any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(sampleReport());

        controller.getPackageAssignmentReport(
                "client-1", null, "search", "COMPLETE", "2026/01/01", "2026-01-31", 0, 20);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(packageAssignmentReportService, times(1)).getPackageAssignmentReport(
                eq("client-1"), isNull(), eq("search"), eq("COMPLETE"),
                fromCaptor.capture(), toCaptor.capture(), eq(0), eq(20));

        assertEquals(LocalDate.of(2026, 1, 1), fromCaptor.getValue());
        assertEquals(LocalDate.of(2026, 1, 31), toCaptor.getValue());
    }

    @Test
    void getPackageAssignmentReport_shouldReturn400WhenToDateBeforeFromDate() {
        ResponseEntity<ApiResponseDto<PackageAssignmentReportDTO>> response =
                controller.getPackageAssignmentReport(
                        null, null, null, null, "2026-03-01", "2026-02-01", 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatusCode());
        assertNull(response.getBody().getData());
    }

    @Test
    void exportPackageAssignmentReport_shouldReturnCsvAttachment() {
        when(packageAssignmentReportService.exportPackageAssignmentReportCsv(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn("\uFEFFUser,Package\n".getBytes());

        ResponseEntity<Resource> response = controller.exportPackageAssignmentReport(
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("text/csv; charset=UTF-8", response.getHeaders().getFirst("Content-Type"));
        assertNotNull(response.getHeaders().getFirst("Content-Disposition"));
        assertNotNull(response.getBody());
    }

    @Test
    void exportPackageAssignmentReport_shouldPassCompleteStatusFilterToService() {
        when(packageAssignmentReportService.exportPackageAssignmentReportCsv(
                any(), any(), any(), any(), any(), any()))
                .thenReturn(new byte[0]);

        controller.exportPackageAssignmentReport(
                "client-1", null, null, "COMPLETE", "2026-01-01", "2026-01-31");

        verify(packageAssignmentReportService).exportPackageAssignmentReportCsv(
                eq("client-1"), isNull(), isNull(), eq("COMPLETE"),
                eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31)));
    }

    @Test
    void exportPackageAssignmentReport_shouldReturn400WhenDateRangeInvalid() {
        ResponseEntity<Resource> response = controller.exportPackageAssignmentReport(
                null, null, null, null, "2026-05-10", "2026-05-01");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private static PackageAssignmentReportDTO sampleReport() {
        return PackageAssignmentReportDTO.builder()
                .summary(PackageAssignmentSummaryDTO.builder()
                        .totalPackages(2L)
                        .totalSubPackages(10L)
                        .activeAssignments(7L)
                        .expiringSoon(2L)
                        .expired(1L)
                        .completeAssignments(3L)
                        .build())
                .assignmentLog(new AllResponseDto<>(0, 20, 10L, List.of()))
                .build();
    }
}
