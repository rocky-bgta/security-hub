package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.dto.enums.QuickRange;
import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserTopicProgressReportRepositoryCustom;
import com.aspire.asat.cms.service.reports.ClientReportScope;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.service.reports.TopicAssignmentReportService;
import com.aspire.asat.cms.util.QuickRangeResolver;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicAssignmentReportServiceImpl implements TopicAssignmentReportService {

    private final ClientReportScopeResolver clientReportScopeResolver;
    private final UserCurrentContextService userCurrentContextService;
    private final UserTopicProgressReportRepositoryCustom userTopicProgressReportRepository;
    private final SubPackageRepositoryCustom subPackageRepositoryCustom;

    @Override
    public TopicAssignmentSummaryDTO getSummary(
            String search,
            String clientAdminId,
            String mspId,
            String startDate,
            String endDate,
            QuickRange quickRange) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);
        if (scope.isEmpty()) {
            return emptySummary();
        }

        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        Instant start = parseStart(dates[0]);
        Instant end = parseEnd(dates[1]);
        List<String> topicIdsFilter = resolveTopicIdsFilter(search);

        TopicAssignmentSummaryDTO metrics = userTopicProgressReportRepository.aggregateSummaryMetrics(
                scope.clientAdminId(), scope.clientAdminIds(), start, end, topicIdsFilter);

        long totalTopics = subPackageRepositoryCustom.countDistinctTopicsInSubPackages(
                scope.clientAdminId(), scope.clientAdminIds());

        long assignedTopics = metrics.getAssignedTopics() != null ? metrics.getAssignedTopics() : 0L;
        long unassigned = Math.max(0L, totalTopics - assignedTopics);

        return TopicAssignmentSummaryDTO.builder()
                .totalTopics(totalTopics)
                .assignedTopics(assignedTopics)
                .unassignedTopics(unassigned)
                .assignedToUsers(metrics.getAssignedToUsers() != null ? metrics.getAssignedToUsers() : 0L)
                .completedAssignments(metrics.getCompletedAssignments() != null ? metrics.getCompletedAssignments() : 0L)
                .avgProgress(metrics.getAvgProgress() != null ? metrics.getAvgProgress() : 0.0)
                .build();
    }

    @Override
    public List<TopicAssignmentProgressRowDTO> getTopicProgressList(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange,
            int offset,
            int pageSize) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);
        if (scope.isEmpty()) {
            return List.of();
        }

        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        Instant start = parseStart(dates[0]);
        Instant end = parseEnd(dates[1]);
        List<String> topicIdsFilter = resolveTopicIdsFilter(search);

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        int skip = safeOffset * safePageSize;

        return userTopicProgressReportRepository.aggregateTopicProgressPage(
                scope.clientAdminId(), scope.clientAdminIds(), start, end, statuses, topicIdsFilter,
                skip, safePageSize);
    }

    @Override
    public long countTopicProgress(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);
        if (scope.isEmpty()) {
            return 0L;
        }

        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        Instant start = parseStart(dates[0]);
        Instant end = parseEnd(dates[1]);
        List<String> topicIdsFilter = resolveTopicIdsFilter(search);

        return userTopicProgressReportRepository.countTopicProgressGroups(
                scope.clientAdminId(), scope.clientAdminIds(), start, end, statuses, topicIdsFilter);
    }

    @Override
    public byte[] exportCsv(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);
        if (scope.isEmpty()) {
            return buildCsv(List.of());
        }

        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        Instant start = parseStart(dates[0]);
        Instant end = parseEnd(dates[1]);
        List<String> topicIdsFilter = resolveTopicIdsFilter(search);

        List<TopicAssignmentProgressRowDTO> rows = userTopicProgressReportRepository.aggregateTopicProgressForExport(
                scope.clientAdminId(), scope.clientAdminIds(), start, end, statuses, topicIdsFilter);
        return buildCsv(rows);
    }

    private List<String> resolveTopicIdsFilter(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        return userTopicProgressReportRepository.findTopicIdsByNameSearch(search);
    }

    private static TopicAssignmentSummaryDTO emptySummary() {
        return TopicAssignmentSummaryDTO.builder()
                .totalTopics(0L)
                .assignedTopics(0L)
                .unassignedTopics(0L)
                .assignedToUsers(0L)
                .completedAssignments(0L)
                .avgProgress(0.0)
                .build();
    }

    private static Instant parseStart(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return LocalDate.parse(date.trim()).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static Instant parseEnd(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return LocalDate.parse(date.trim()).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
    }

    private static byte[] buildCsv(List<TopicAssignmentProgressRowDTO> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Topic ID,Topic Name,Assigned Count,Completed Count,Avg Progress %,Completion Rate %\n");
        for (TopicAssignmentProgressRowDTO row : rows) {
            sb.append(escapeCsv(row.getTopicId())).append(',');
            sb.append(escapeCsv(row.getTopicName())).append(',');
            sb.append(row.getAssignedCount() != null ? row.getAssignedCount() : 0).append(',');
            sb.append(row.getCompletedCount() != null ? row.getCompletedCount() : 0).append(',');
            sb.append(formatDouble(row.getAvgProgress())).append(',');
            sb.append(formatDouble(row.getCompletionRate())).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String formatDouble(Double value) {
        return value == null ? "0.00" : String.format("%.2f", value);
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
