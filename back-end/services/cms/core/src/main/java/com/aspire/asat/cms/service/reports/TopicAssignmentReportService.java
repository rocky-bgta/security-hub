package com.aspire.asat.cms.service.reports;

import com.aspire.asat.cms.dto.enums.QuickRange;
import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;

import java.util.List;

public interface TopicAssignmentReportService {

    TopicAssignmentSummaryDTO getSummary(
            String search,
            String clientAdminId,
            String mspId,
            String startDate,
            String endDate,
            QuickRange quickRange);

    List<TopicAssignmentProgressRowDTO> getTopicProgressList(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange,
            int offset,
            int pageSize);

    long countTopicProgress(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange);

    byte[] exportCsv(
            String search,
            String clientAdminId,
            String mspId,
            List<String> statuses,
            String startDate,
            String endDate,
            QuickRange quickRange);
}
