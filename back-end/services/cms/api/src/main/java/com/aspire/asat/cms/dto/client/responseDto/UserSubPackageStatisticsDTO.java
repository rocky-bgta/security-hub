package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user subpackage statistics response
 * Contains counts of subpackages by status for user dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubPackageStatisticsDTO {
    
    /**
     * Total number of subpackages assigned to the user
     */
    private int total;
    
    /**
     * Number of completed subpackages (status = COMPLETED)
     */
    private int completed;
    
    /**
     * Number of subpackages ready for exam (status = EXAM)
     */
    private int exam;
    
    /**
     * Number of subpackages in progress (status = IN_PROGRESS)
     */
    private int inProgress;
    
    /**
     * Number of subpackages not started (status = NOT_STARTED)
     */
    private int notStarted;
}
