package com.aspire.asat.cms.controller.reports.impl;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.service.reports.CertificateReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateReportControllerImplTest {

    @Mock
    private CertificateReportService certificateReportService;

    @InjectMocks
    private CertificateReportControllerImpl controller;

    @Test
    void getExpiredCertificateReportSummary_IsoLocalDate_ParsesStartAndEndOfDay() {
        when(certificateReportService.getExpiredCertificateReportSummary(any(), any(), any(), any(), any()))
                .thenReturn(new ExpiredCertificateReportSummaryDTO());

        ResponseEntity<ApiResponseDto<ExpiredCertificateReportSummaryDTO>> response =
                controller.getExpiredCertificateReportSummary("client-1", null, "2026-06-26", "2026-06-26", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(certificateReportService).getExpiredCertificateReportSummary(
                eq("client-1"), isNull(), fromCaptor.capture(), toCaptor.capture(), isNull());

        assertEquals(Instant.parse("2026-06-26T00:00:00Z"), fromCaptor.getValue());
        assertEquals(Instant.parse("2026-06-27T00:00:00Z").minusNanos(1), toCaptor.getValue());
    }

    @Test
    void getExpiredCertificateReportSummary_SlashDate_StillSupported() {
        when(certificateReportService.getExpiredCertificateReportSummary(any(), any(), any(), any(), any()))
                .thenReturn(new ExpiredCertificateReportSummaryDTO());

        controller.getExpiredCertificateReportSummary("client-1", null, "2026/06/26", "2026/06/26", null);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(certificateReportService).getExpiredCertificateReportSummary(
                eq("client-1"), isNull(), fromCaptor.capture(), any(), isNull());
        assertEquals(Instant.parse("2026-06-26T00:00:00Z"), fromCaptor.getValue());
    }

    @Test
    void getExpiredCertificateReportSummary_InvalidDate_ReturnsBadRequest() {
        ResponseEntity<ApiResponseDto<ExpiredCertificateReportSummaryDTO>> response =
                controller.getExpiredCertificateReportSummary("client-1", null, "26-06-2026", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getExpiredCertificateReportRows_ForwardsStatusFilter() {
        when(certificateReportService.getExpiredCertificateReportRows(
                any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        controller.getExpiredCertificateReportRows(
                "client-1", null, null, null, null, CertificateStatus.EXPIRED, null, 0, 10);

        verify(certificateReportService).getExpiredCertificateReportRows(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(),
                eq(CertificateStatus.EXPIRED), isNull(), eq(0), eq(10));
    }

    @Test
    void getIssuedCertificateReportSummary_Success() {
        when(certificateReportService.getIssuedCertificateReportSummary(any(), any(), any(), any()))
                .thenReturn(IssuedCertificateReportSummaryDTO.builder()
                        .totalIssued(10L).thisMonth(2L).thisQuarter(5L).build());

        ResponseEntity<ApiResponseDto<IssuedCertificateReportSummaryDTO>> response =
                controller.getIssuedCertificateReportSummary("client-1", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getData().getTotalIssued());
    }
}
