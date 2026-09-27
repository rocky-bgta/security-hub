package com.aspire.asat.cms.controller.reports;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.enums.QuickRange;
import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Topic Assignment Report",
        description = "Topics assigned to users with summary KPIs and per-topic progress")
@RequestMapping(WebApiUrlConstants.TOPIC_ASSIGNMENT_REPORT_API)
public interface TopicAssignmentReportController {

    @Operation(
            summary = "Get topic assignment report summary",
            description = "Returns KPI cards: total/assigned/unassigned topics, assignment counts, "
                    + "completed assignments, and average progress. "
                    + "Status filter is not applied to summary. "
                    + "When quickRange is provided it takes precedence over startDate/endDate. "
                    + "Date filter uses user_topics.createdAt."
    )
    @GetMapping("/summary")
    ResponseEntity<ApiResponseDto<TopicAssignmentSummaryDTO>> getSummary(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) QuickRange quickRange
    );

    @Operation(
            summary = "Get topic assignment progress list",
            description = "Returns a paginated list of topics with assigned/completed counts and progress. "
                    + "status filter accepts NOT_STARTED, IN_PROGRESS, COMPLETED (assignment progress). "
                    + "When quickRange is provided it takes precedence over startDate/endDate. "
                    + "offset is a 0-based page index (skip = offset * pageSize)."
    )
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicAssignmentProgressRowDTO>>>> getList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Assignment progress statuses: NOT_STARTED, IN_PROGRESS, COMPLETED")
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) QuickRange quickRange,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) int pageSize
    );

    @Operation(
            summary = "Export topic assignment report as CSV",
            description = "Downloads per-topic progress rows using the same filters as the list endpoint."
    )
    @GetMapping("/export/csv")
    ResponseEntity<Resource> exportCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) QuickRange quickRange
    );
}
