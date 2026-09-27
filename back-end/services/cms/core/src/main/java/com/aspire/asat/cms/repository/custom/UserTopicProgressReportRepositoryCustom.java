package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;

import java.time.Instant;
import java.util.List;

public interface UserTopicProgressReportRepositoryCustom {

    List<String> findTopicIdsByNameSearch(String search);

    TopicAssignmentSummaryDTO aggregateSummaryMetrics(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> topicIdsFilter);

    List<TopicAssignmentProgressRowDTO> aggregateTopicProgressPage(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter,
            int skip,
            int limit);

    long countTopicProgressGroups(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter);

    List<TopicAssignmentProgressRowDTO> aggregateTopicProgressForExport(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter);
}
