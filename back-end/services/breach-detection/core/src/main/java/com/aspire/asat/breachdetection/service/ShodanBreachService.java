package com.aspire.asat.breachdetection.service;

import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.dto.request.AddShodanMonitorRequest;
import com.aspire.asat.breachdetection.dto.response.ImpersonationTacticsSummaryDto;
import com.aspire.asat.breachdetection.dto.response.ShodanAlertDto;
import com.aspire.asat.breachdetection.dto.response.ShodanAlertTableRowDto;
import com.aspire.asat.breachdetection.dto.response.ShodanMonitorDto;
import com.aspire.asat.breachdetection.dto.response.ShodanOverviewDto;
import com.aspire.asat.breachdetection.dto.response.ShodanRiskTrendResponseDto;
import com.aspire.asat.breachdetection.dto.response.ShodanSyncResultDto;
import com.aspire.asat.breachdetection.dto.response.ShodanTacticDistributionDto;

import java.util.List;

public interface ShodanBreachService {

    // --- Monitors ---

    ShodanMonitorDto addMonitor(AddShodanMonitorRequest request);

    void deleteMonitor(String id);

    List<ShodanMonitorDto> listMonitors(ShodanSubjectType subjectType);

    // --- Sync ---

    ShodanSyncResultDto triggerManualSync(ShodanSubjectType subjectType);

    // --- Read APIs for dashboard ---

    ShodanOverviewDto getOverview(ShodanSubjectType subjectType);

    AllResponseDto<List<ShodanAlertDto>> getAlerts(ShodanSubjectType subjectType,
                                                   int offset,
                                                   int pageSize,
                                                   String sortBy,
                                                   String sortDirection,
                                                   ShodanAlertStatus status,
                                                   ShodanAlertSeverity severity,
                                                   ShodanAlertType alertType);

    AllResponseDto<List<ShodanAlertTableRowDto>> getAlertsTable(ShodanSubjectType subjectType,
                                                                int offset,
                                                                int pageSize,
                                                                String sortBy,
                                                                String sortDirection,
                                                                ShodanAlertStatus status,
                                                                ShodanAlertSeverity severity,
                                                                ShodanAlertType alertType);

    ShodanAlertDto getAlertById(String id);

    ShodanAlertDto updateAlertStatus(String id, ShodanAlertStatus status);

    ShodanRiskTrendResponseDto getRiskTrend(ShodanSubjectType subjectType, int days);

    ShodanTacticDistributionDto getTacticDistribution(ShodanSubjectType subjectType);

    ImpersonationTacticsSummaryDto getImpersonationTactics(ShodanSubjectType subjectType);
}
