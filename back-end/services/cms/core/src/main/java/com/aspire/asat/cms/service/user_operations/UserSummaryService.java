package com.aspire.asat.cms.service.user_operations;

import com.aspire.asat.cms.dto.client.responseDto.DashboardSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserPackageGroupedResponseDTO;

public interface UserSummaryService {
    
    /**
     * Gets dashboard summary for a user
     * @param userId The user ID
     * @return Dashboard summary response DTO
     */
    DashboardSummaryResponseDTO getDashboardSummary(String userId);
    
    /**
     * Gets user packages grouped by status
     * @param userId The user ID
     * @return User package grouped response DTO
     */
    UserPackageGroupedResponseDTO getGroupedUserPackages(String userId);
}
