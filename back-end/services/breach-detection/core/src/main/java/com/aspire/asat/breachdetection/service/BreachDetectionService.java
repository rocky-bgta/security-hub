package com.aspire.asat.breachdetection.service;

import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import com.aspire.asat.breachdetection.dto.request.BreachConfigRequest;
import com.aspire.asat.breachdetection.dto.request.InsecureWebBreachSearchRequest;
import com.aspire.asat.breachdetection.dto.response.BreachActivityResponseDto;
import com.aspire.asat.breachdetection.dto.response.BreachConfigDto;
import com.aspire.asat.breachdetection.dto.response.BreachSyncResultDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebBreachFindingDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebFindingsSummaryDto;
import com.aspire.asat.breachdetection.model.BreachDetectionConfig;

import java.util.List;

public interface BreachDetectionService {
    BreachConfigDto getConfig();

    BreachConfigDto updateConfig(BreachConfigRequest request);

    BreachSyncResultDto triggerManualSync();

    void triggerScheduledSync(BreachDetectionConfig config);

    AllResponseDto<List<InsecureWebBreachFindingDto>> getFindings(
            int offset, int pageSize, String sortBy, String sortDirection,
            String fromDate, String toDate, String email, String domain, InsecureWebBreachStatus breachStatus);

    InsecureWebBreachFindingDto getFindingById(String id);

    /**
     * Returns daily counts of email breach findings for the current client over
     * the last {@code days} days, broken down by severity (CRITICAL/HIGH/MEDIUM/LOW).
     * Allowed values: 30, 60, 90 (default 30 if invalid).
     */
    BreachActivityResponseDto getEmailBreachActivity(int days);

    InsecureWebFindingsSummaryDto getFindingsSummary();

    /**
     * Live search against the upstream InsecureWeb dark-web API for the current
     * client's organization. Supports the operator-suffix syntax
     * ({@code breachStatus.in}, {@code domain.contains}, {@code email.contains}).
     * Results are NOT persisted; this is a pass-through search for the UI.
     */
    AllResponseDto<List<InsecureWebBreachFindingDto>> searchExternalBreaches(InsecureWebBreachSearchRequest request);

    AllResponseDto<List<String>> getSources(int offset, int pageSize, String sortBy, String sortDirection);
}
