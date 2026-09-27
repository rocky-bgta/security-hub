package com.aspire.asat.universal.service;

import com.aspire.asat.universal.supportTicket.response.SupportResolutionTimeResponseDto;

public interface SupportResolutionTimeService {

    SupportResolutionTimeResponseDto getSupportResolutionTime(String clientAdminId);

    byte[] exportSupportResolutionTimeCsv(String clientAdminId);
}
