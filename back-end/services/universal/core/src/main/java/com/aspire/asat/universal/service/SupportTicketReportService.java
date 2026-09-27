package com.aspire.asat.universal.service;

import com.aspire.asat.universal.supportTicket.response.SupportTicketRecentRowDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketReportSummaryDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketStatusDistributionDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;

import java.time.Instant;
import java.util.List;

public interface SupportTicketReportService {
    SupportTicketReportSummaryDto getOpenVsClosedSummary(
            String clientAdminId,
            Instant fromDate,
            Instant toDate,
            String search,
            Integer thresholdDays);

    List<SupportTicketStatusDistributionDto> getOpenVsClosedStatusDistribution(
            String clientAdminId,
            Instant fromDate,
            Instant toDate,
            String search,
            Integer thresholdDays);

    AllResponseDto<List<SupportTicketRecentRowDto>> getOpenVsClosedRecentTickets(
            String clientAdminId,
            Instant fromDate,
            Instant toDate,
            String search,
            Integer thresholdDays,
            int offset,
            int pageSize);

    byte[] exportOpenVsClosedReport(
            String clientAdminId,
            Instant fromDate,
            Instant toDate,
            String search,
            Integer thresholdDays);
}
