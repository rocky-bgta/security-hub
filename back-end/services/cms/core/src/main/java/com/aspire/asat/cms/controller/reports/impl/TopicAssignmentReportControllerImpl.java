package com.aspire.asat.cms.controller.reports.impl;

import com.aspire.asat.cms.controller.reports.TopicAssignmentReportController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.enums.QuickRange;
import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;
import com.aspire.asat.cms.service.reports.TopicAssignmentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TopicAssignmentReportControllerImpl implements TopicAssignmentReportController {

    private final TopicAssignmentReportService topicAssignmentReportService;

    @Override
    public ResponseEntity<ApiResponseDto<TopicAssignmentSummaryDTO>> getSummary(
            String search,
            String clientAdminId,
            String mspId,
            String startDate,
            String endDate,
            QuickRange quickRange) {
        log.info("Topic assignment summary - search: {}, clientAdminId: {}, mspId: {}, startDate: {}, endDate: {}, quickRange: {}",
                search, clientAdminId, mspId, startDate, endDate, quickRange);

        TopicAssignmentSummaryDTO summary = topicAssignmentReportService.getSummary(
                search, clientAdminId, mspId, startDate, endDate, quickRange);
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Topic assignment summary retrieved successfully", 200, summary));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicAssignmentProgressRowDTO>>>> getList(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange,
            int offset,
            int pageSize) {
        log.info("Topic assignment list - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, startDate: {}, endDate: {}, quickRange: {}, offset: {}, pageSize: {}",
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange, offset, pageSize);

        int effectivePageSize = Math.min(Math.max(pageSize, 1), 100);
        List<TopicAssignmentProgressRowDTO> items = topicAssignmentReportService.getTopicProgressList(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange, offset, effectivePageSize);
        long total = topicAssignmentReportService.countTopicProgress(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);

        AllResponseDto<List<TopicAssignmentProgressRowDTO>> page =
                new AllResponseDto<>(offset, effectivePageSize, total, items);
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Topic assignment progress list retrieved successfully", 200, page));
    }

    @Override
    public ResponseEntity<Resource> exportCsv(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange) {
        log.info("Topic assignment CSV export - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, startDate: {}, endDate: {}, quickRange: {}",
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);

        byte[] csv = topicAssignmentReportService.exportCsv(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);
        ByteArrayResource resource = new ByteArrayResource(csv);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=topic-assignment-report-" + System.currentTimeMillis() + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(resource);
    }
}
