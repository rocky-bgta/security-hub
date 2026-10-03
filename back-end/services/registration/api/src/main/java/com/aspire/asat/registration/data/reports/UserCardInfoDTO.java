package com.aspire.asat.registration.data.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite payload for the User Summary Report endpoint. Bundles the summary
 * cards, the growth-trend chart series and the paginated user details table
 * into a single response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCardInfoDTO {
    private Long totalMsp;
    private Long totalClientAdmin;
    private Long totalLicenseUser;
    private Long totalActiveUser;
    private Long totalSuspendedUser;
}
