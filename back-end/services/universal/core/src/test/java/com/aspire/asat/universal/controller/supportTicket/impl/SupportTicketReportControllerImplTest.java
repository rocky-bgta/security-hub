package com.aspire.asat.universal.controller.supportTicket.impl;

import com.aspire.asat.universal.service.SupportTicketReportService;
import com.aspire.asat.universal.supportTicket.response.SupportTicketReportSummaryDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketReportControllerImplTest {

    @Mock
    private SupportTicketReportService supportTicketReportService;

    @InjectMocks
    private SupportTicketReportControllerImpl controller;

    @Test
    void getOpenVsClosedSummary_IsoLocalDate_ParsesStartAndEndOfDay() {
        when(supportTicketReportService.getOpenVsClosedSummary(any(), any(), any(), any(), any()))
                .thenReturn(new SupportTicketReportSummaryDto());

        ResponseEntity<ApiResponseDto<SupportTicketReportSummaryDto>> response =
                controller.getOpenVsClosedSummary("client-1", "2026-06-26", "2026-06-26", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(supportTicketReportService).getOpenVsClosedSummary(
                eq("client-1"), fromCaptor.capture(), toCaptor.capture(), isNull(), isNull());

        assertEquals(Instant.parse("2026-06-26T00:00:00Z"), fromCaptor.getValue());
        assertEquals(Instant.parse("2026-06-27T00:00:00Z").minusNanos(1), toCaptor.getValue());
    }

    @Test
    void getOpenVsClosedSummary_SlashDate_StillSupported() {
        when(supportTicketReportService.getOpenVsClosedSummary(any(), any(), any(), any(), any()))
                .thenReturn(new SupportTicketReportSummaryDto());

        controller.getOpenVsClosedSummary("client-1", "2026/06/26", "2026/06/26", null, null);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(supportTicketReportService).getOpenVsClosedSummary(
                eq("client-1"), fromCaptor.capture(), any(), isNull(), isNull());
        assertEquals(Instant.parse("2026-06-26T00:00:00Z"), fromCaptor.getValue());
    }

    @Test
    void getOpenVsClosedSummary_InvalidDate_ReturnsBadRequest() {
        ResponseEntity<ApiResponseDto<SupportTicketReportSummaryDto>> response =
                controller.getOpenVsClosedSummary("client-1", "26-06-2026", null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
