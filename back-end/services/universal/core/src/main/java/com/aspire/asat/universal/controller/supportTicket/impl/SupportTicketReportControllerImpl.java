package com.aspire.asat.universal.controller.supportTicket.impl;

import com.aspire.asat.universal.controller.supportTicket.SupportTicketReportController;
import com.aspire.asat.universal.service.SupportTicketReportService;
import com.aspire.asat.universal.supportTicket.response.SupportTicketRecentRowDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketReportSummaryDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketStatusDistributionDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SupportTicketReportControllerImpl implements SupportTicketReportController {
    private static final DateTimeFormatter FRONTEND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final SupportTicketReportService supportTicketReportService;

    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketReportSummaryDto>> getOpenVsClosedSummary(
            String clientAdminId, String fromDate, String toDate, String search, Integer thresholdDays) {
        try {
            Instant from = parseFlexibleDate(fromDate, false);
            Instant to = parseFlexibleDate(toDate, true);
            SupportTicketReportSummaryDto summary = supportTicketReportService.getOpenVsClosedSummary(
                    clientAdminId, from, to, search, thresholdDays);
            return ResponseEntity.ok(new ApiResponseDto<>("Support ticket report summary fetched successfully", 200, summary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get support ticket report summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get support ticket report summary: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<SupportTicketStatusDistributionDto>>> getOpenVsClosedStatusDistribution(
            String clientAdminId, String fromDate, String toDate, String search, Integer thresholdDays) {
        try {
            Instant from = parseFlexibleDate(fromDate, false);
            Instant to = parseFlexibleDate(toDate, true);
            List<SupportTicketStatusDistributionDto> distribution = supportTicketReportService.getOpenVsClosedStatusDistribution(
                    clientAdminId, from, to, search, thresholdDays);
            return ResponseEntity.ok(new ApiResponseDto<>("Support ticket status distribution fetched successfully", 200, distribution));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get support ticket status distribution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get support ticket status distribution: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SupportTicketRecentRowDto>>>> getOpenVsClosedRecentTickets(
            String clientAdminId, String fromDate, String toDate, String search, Integer thresholdDays, int offset, int pageSize) {
        try {
            Instant from = parseFlexibleDate(fromDate, false);
            Instant to = parseFlexibleDate(toDate, true);
            AllResponseDto<List<SupportTicketRecentRowDto>> recentTickets = supportTicketReportService.getOpenVsClosedRecentTickets(
                    clientAdminId, from, to, search, thresholdDays, offset, pageSize);
            return ResponseEntity.ok(new ApiResponseDto<>("Support ticket recent tickets fetched successfully", 200, recentTickets));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get support ticket recent tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get support ticket recent tickets: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public void exportOpenVsClosedReport(
            String clientAdminId, String fromDate, String toDate, String search, Integer thresholdDays, HttpServletResponse response) {
        try {
            Instant from = parseFlexibleDate(fromDate, false);
            Instant to = parseFlexibleDate(toDate, true);
            byte[] csvBytes = supportTicketReportService.exportOpenVsClosedReport(
                    clientAdminId, from, to, search, thresholdDays);
            response.setContentType("text/csv");
            String fileName = "Support_Ticket_Report_" + Instant.now().toEpochMilli() + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLength(csvBytes.length);
            response.getOutputStream().write(csvBytes);
            response.getOutputStream().flush();
        } catch (IllegalArgumentException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        } catch (Exception e) {
            log.error("Failed to export support ticket report", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to export support ticket report: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    private Instant parseFlexibleDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return localDateToInstant(LocalDate.parse(value), endOfDay);
            } catch (DateTimeParseException ignoredIso) {
                try {
                    return localDateToInstant(LocalDate.parse(value, FRONTEND_DATE_FORMAT), endOfDay);
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd, yyyy/MM/dd, or ISO-8601");
                }
            }
        }
    }

    private Instant localDateToInstant(LocalDate date, boolean endOfDay) {
        return endOfDay
                ? date.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC)
                : date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
