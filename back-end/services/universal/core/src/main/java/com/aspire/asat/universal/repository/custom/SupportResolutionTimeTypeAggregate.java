package com.aspire.asat.universal.repository.custom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportResolutionTimeTypeAggregate {

    private String supportTypeId;
    private long totalTickets;
    private long openTickets;
    private long closedTickets;
    private long inProgressTickets;
    /** Sum of (updatedDate - createdDate) in milliseconds for CLOSED tickets. */
    private long totalClosedResolutionMs;
}
