package com.aspire.asat.registration.service.userActivity;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.userActivity.UserLoginHistoryResponseDTO;
import com.aspire.asat.registration.data.userActivity.UserLoginStatisticsResponseDTO;
import com.aspire.asat.registration.enums.LoginHistoryFilterType;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for User Activity operations
 */
public interface UserActivityService {

    /**
     * Get user login statistics report data based on a filter type
     * 
     * @param filterType Filter type for the report (7Day, 1. Month, 12. Month)
     * @return List of login statistics based on a filter type
     */
    List<UserLoginStatisticsResponseDTO> getUserLoginStatistics(LoginHistoryFilterType filterType);

    /**
     * Get paginated user login/logout history for audit trail
     * 
     * @param actionType Optional filter by action type (LOGIN, LOGOUT)
     * @param startDate Optional start date filter (will be set to Beginning of Day - 00:00:00)
     * @param endDate Optional end date filter (will be set to End of Day - 23:59:59)
     * @param username Optional username search filter
     * @param offset Page offset (0-based)
     * @param pageSize Page size
     * @return Paginated list of user login history using standard AllResponseDto format
     */
    AllResponseDto<List<UserLoginHistoryResponseDTO>> getUserLoginHistory(String actionType, LocalDate startDate, 
                                                                         LocalDate endDate, String username, int offset, int pageSize);
}
