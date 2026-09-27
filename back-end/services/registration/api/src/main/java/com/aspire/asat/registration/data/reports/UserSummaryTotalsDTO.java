package com.aspire.asat.registration.data.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregate counts that power the four summary cards in the User Summary Report.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryTotalsDTO {

    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long newSignupsLast30Days;
}
