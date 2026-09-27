package com.aspire.asat.cms.service.clientAdminProductUsageReport;

import com.aspire.asat.cms.dto.clientAdminProductUsageReport.ClientAdminProductUsageReportResponseDto;

public interface ClientAdminProductUsageReportService {

    ClientAdminProductUsageReportResponseDto getClientAdminProductUsageReport(String clientAdminId);

    /**
     * Exports the products section of the client admin product usage report as CSV bytes.
     */
    byte[] exportClientAdminProductUsageReportCsv(String clientAdminId);
}
