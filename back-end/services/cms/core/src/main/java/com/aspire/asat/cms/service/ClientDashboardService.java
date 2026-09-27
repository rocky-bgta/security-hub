package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardRequestDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardResponseDto;

public interface ClientDashboardService {

    /**
     * Create or update client dashboard data
     * If clientAdminId doesn't exist, creates new record
     * If clientAdminId exists, updates existing record
     * @param requestDto the client dashboard request data
     * @return ClientDashboardResponseDto
     */
    ClientDashboardResponseDto createOrUpdateClientDashboard(ClientDashboardRequestDto requestDto);

    /**
     * Get client dashboard data by client admin ID
     * @param clientAdminId the client admin ID
     * @return ClientDashboardResponseDto
     */
    ClientDashboardResponseDto getClientDashboardByClientAdminId(String clientAdminId);
}
