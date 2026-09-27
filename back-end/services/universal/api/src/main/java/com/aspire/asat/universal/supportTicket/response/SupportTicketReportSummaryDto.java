package com.aspire.asat.universal.supportTicket.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketReportSummaryDto {
    private long totalTickets;
    private long openTickets;
    private long closedTickets;
    private long highPriorityTickets;
}
