package com.aspire.asat.universal.supportTicket.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Overall support resolution time and SLA compliance metrics")
public class SupportResolutionTimeResponseDto {

    @Schema(description = "Average resolution time in hours for CLOSED tickets (updatedDate - createdDate)", example = "5.0")
    private double averageResolutionTime;

    @Schema(description = "SLA compliance percentage (closed tickets / total tickets * 100)", example = "50.0")
    private double slaCompliance;

    @Schema(description = "Total support tickets in scope", example = "4")
    private long totalTickets;

    @Schema(description = "Total CLOSED support tickets in scope", example = "2")
    private long closedTickets;

    @Schema(description = "Open support tickets in scope (totalTickets - closedTickets)", example = "2")
    private long openedTickets;


    @Schema(description = "Resolution time metrics broken down by support ticket type")
    private List<SupportResolutionTimeByTypeDto> bySupportType;
}
