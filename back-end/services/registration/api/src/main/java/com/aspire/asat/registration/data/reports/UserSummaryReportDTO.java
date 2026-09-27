package com.aspire.asat.registration.data.reports;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Composite payload for the User Summary Report endpoint. Bundles the summary
 * cards, the growth-trend chart series and the paginated user details table
 * into a single response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryReportDTO {

    private UserSummaryTotalsDTO totals;
    private List<UserGrowthTrendPointDTO> growthTrend;
    private AllResponseDto<List<UserDetailRowDTO>> details;
}
