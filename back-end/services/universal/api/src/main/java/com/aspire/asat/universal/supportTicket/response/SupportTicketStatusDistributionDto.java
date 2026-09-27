package com.aspire.asat.universal.supportTicket.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketStatusDistributionDto {
    private String status;
    private long count;
}
