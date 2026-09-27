package com.aspire.asat.breachdetection.controller.impl;

import com.aspire.asat.breachdetection.controller.ShodanBreachController;
import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.ApiResponseDto;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
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
import com.aspire.asat.breachdetection.service.ShodanBreachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ShodanBreachControllerImpl implements ShodanBreachController {

    private final ShodanBreachService shodanBreachService;

    @Override
    public ResponseEntity<ApiResponseDto<ShodanMonitorDto>> addMonitor(AddShodanMonitorRequest request) {
        try {
            ShodanMonitorDto data = shodanBreachService.addMonitor(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.<ShodanMonitorDto>builder()
                            .message("Monitor added successfully")
                            .data(data)
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.<ShodanMonitorDto>builder()
                    .message(e.getMessage()).build());
        } catch (Exception e) {
            log.error("Failed to add monitor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanMonitorDto>builder()
                            .message("Failed to add monitor: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteMonitor(String id) {
        try {
            shodanBreachService.deleteMonitor(id);
            return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                    .message("Monitor deleted successfully").build());
        } catch (Exception e) {
            log.error("Failed to delete monitor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<Void>builder()
                            .message("Failed to delete monitor: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ShodanMonitorDto>>> listMonitors(ShodanSubjectType subjectType) {
        try {
            List<ShodanMonitorDto> data = shodanBreachService.listMonitors(subjectType);
            return ResponseEntity.ok(ApiResponseDto.<List<ShodanMonitorDto>>builder()
                    .message("Monitors retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to list monitors for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<List<ShodanMonitorDto>>builder()
                            .message("Failed to list monitors: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ShodanSyncResultDto>> triggerSync(ShodanSubjectType subjectType) {
        try {
            ShodanSyncResultDto data = shodanBreachService.triggerManualSync(subjectType);
            return ResponseEntity.ok(ApiResponseDto.<ShodanSyncResultDto>builder()
                    .message("Shodan sync completed").data(data).build());
        } catch (Exception e) {
            log.error("Shodan manual sync failed for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanSyncResultDto>builder()
                            .message("Failed to run sync: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ShodanOverviewDto>> getOverview(ShodanSubjectType subjectType) {
        try {
            ShodanOverviewDto data = shodanBreachService.getOverview(subjectType);
            return ResponseEntity.ok(ApiResponseDto.<ShodanOverviewDto>builder()
                    .message("Overview retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch Shodan overview for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanOverviewDto>builder()
                            .message("Failed to fetch overview: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ShodanAlertDto>>>> getAlerts(ShodanSubjectType subjectType,
                                                                                           int offset,
                                                                                           int pageSize,
                                                                                           String sortBy,
                                                                                           String sortDirection,
                                                                                           ShodanAlertStatus status,
                                                                                           ShodanAlertSeverity severity,
                                                                                           ShodanAlertType alertType) {
        try {
            AllResponseDto<List<ShodanAlertDto>> data =
                    shodanBreachService.getAlerts(
                            subjectType, offset, pageSize, sortBy, sortDirection, status, severity, alertType);
            return ResponseEntity.ok(ApiResponseDto.<AllResponseDto<List<ShodanAlertDto>>>builder()
                    .message("Alerts retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch Shodan alerts for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AllResponseDto<List<ShodanAlertDto>>>builder()
                            .message("Failed to fetch alerts: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ShodanAlertTableRowDto>>>> getAlertsTable(ShodanSubjectType subjectType,
                                                                                                         int offset,
                                                                                                         int pageSize,
                                                                                                         String sortBy,
                                                                                                         String sortDirection,
                                                                                                         ShodanAlertStatus status,
                                                                                                         ShodanAlertSeverity severity,
                                                                                                         ShodanAlertType alertType) {
        try {
            AllResponseDto<List<ShodanAlertTableRowDto>> data =
                    shodanBreachService.getAlertsTable(
                            subjectType, offset, pageSize, sortBy, sortDirection, status, severity, alertType);
            return ResponseEntity.ok(ApiResponseDto.<AllResponseDto<List<ShodanAlertTableRowDto>>>builder()
                    .message("Alerts table retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch Shodan alerts table for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AllResponseDto<List<ShodanAlertTableRowDto>>>builder()
                            .message("Failed to fetch alerts table: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ShodanAlertDto>> getAlertById(String id) {
        try {
            ShodanAlertDto data = shodanBreachService.getAlertById(id);
            return ResponseEntity.ok(ApiResponseDto.<ShodanAlertDto>builder()
                    .message("Alert retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch Shodan alert {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanAlertDto>builder()
                            .message("Failed to fetch alert: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ShodanAlertDto>> updateAlertStatus(String id, ShodanAlertStatus status) {
        try {
            ShodanAlertDto data = shodanBreachService.updateAlertStatus(id, status);
            return ResponseEntity.ok(ApiResponseDto.<ShodanAlertDto>builder()
                    .message("Alert status updated").data(data).build());
        } catch (Exception e) {
            log.error("Failed to update status for alert {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanAlertDto>builder()
                            .message("Failed to update status: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ShodanRiskTrendResponseDto>> getRiskTrend(
            ShodanSubjectType subjectType, int days) {
        try {
            ShodanRiskTrendResponseDto data = shodanBreachService.getRiskTrend(subjectType, days);
            return ResponseEntity.ok(ApiResponseDto.<ShodanRiskTrendResponseDto>builder()
                    .message("Risk trend retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch Shodan risk-trend for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanRiskTrendResponseDto>builder()
                            .message("Failed to fetch risk trend: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ShodanTacticDistributionDto>> getTacticDistribution(
            ShodanSubjectType subjectType) {
        try {
            ShodanTacticDistributionDto data = shodanBreachService.getTacticDistribution(subjectType);
            return ResponseEntity.ok(ApiResponseDto.<ShodanTacticDistributionDto>builder()
                    .message("Tactic distribution retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch Shodan tactic distribution for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ShodanTacticDistributionDto>builder()
                            .message("Failed to fetch tactic distribution: " + e.getMessage()).build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ImpersonationTacticsSummaryDto>> getImpersonationTactics(
            ShodanSubjectType subjectType) {
        try {
            ImpersonationTacticsSummaryDto data = shodanBreachService.getImpersonationTactics(subjectType);
            return ResponseEntity.ok(ApiResponseDto.<ImpersonationTacticsSummaryDto>builder()
                    .message("Impersonation tactics retrieved successfully").data(data).build());
        } catch (Exception e) {
            log.error("Failed to fetch impersonation tactics for {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ImpersonationTacticsSummaryDto>builder()
                            .message("Failed to fetch impersonation tactics: " + e.getMessage()).build());
        }
    }
}
