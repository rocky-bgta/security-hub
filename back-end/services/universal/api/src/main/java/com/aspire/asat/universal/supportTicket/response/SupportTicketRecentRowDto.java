package com.aspire.asat.universal.supportTicket.response;

import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketRecentRowDto {
    private String ticketId;
    private String subject;
    private String user;
    private Priority priority;
    private TicketStatus status;
    private Instant createdDate;
    private String category;
}
