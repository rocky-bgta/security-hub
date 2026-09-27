package com.aspire.asat.universal.supportTicket.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Support resolution time metrics for a single support ticket type")
public class SupportResolutionTimeByTypeDto {

    @Schema(description = "Support ticket type ID", example = "type-billing-1")
    private String supportTypeId;

    @Schema(description = "Support ticket type name", example = "Billing Issue")
    private String supportTypeName;

    @Schema(description = "Average resolution time in hours for CLOSED tickets of this type", example = "5.0")
    private double averageResolutionTime;

    @Schema(description = "Total tickets for this type", example = "4")
    private long totalTickets;

    @Schema(description = "OPEN tickets for this type", example = "1")
    private long openTickets;

    @Schema(description = "CLOSED tickets for this type", example = "2")
    private long closedTickets;

    @Schema(description = "IN_PROGRESS tickets for this type", example = "1")
    private long inProgressTickets;

    @Schema(description = "SLA compliance percentage for this type (closed / total * 100)", example = "50.0")
    private double slaCompliance;
}
