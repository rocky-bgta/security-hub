package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.dto.enums.QuickRange;
import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserTopicProgressReportRepositoryCustom;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopicAssignmentReportServiceImplTest {

    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private UserTopicProgressReportRepositoryCustom userTopicProgressReportRepository;
    @Mock
    private SubPackageRepositoryCustom subPackageRepositoryCustom;
    @Spy
    private ClientReportScopeResolver clientReportScopeResolver = new ClientReportScopeResolver();

    @InjectMocks
    private TopicAssignmentReportServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(aspireAdminContext());
    }

    @Test
    void getSummary_composesKpisAndUnassigned() {
        when(userTopicProgressReportRepository.aggregateSummaryMetrics(
                isNull(), isNull(), any(), any(), isNull()))
                .thenReturn(TopicAssignmentSummaryDTO.builder()
                        .assignedTopics(60L)
                        .assignedToUsers(2340L)
                        .completedAssignments(1567L)
                        .avgProgress(72.0)
                        .build());
        when(subPackageRepositoryCustom.countDistinctTopicsInSubPackages(isNull(), isNull()))
                .thenReturn(67L);

        TopicAssignmentSummaryDTO summary = service.getSummary(
                null, null, null, "2026-01-01", "2026-01-31", null);

        assertEquals(67L, summary.getTotalTopics());
        assertEquals(60L, summary.getAssignedTopics());
        assertEquals(7L, summary.getUnassignedTopics());
        assertEquals(2340L, summary.getAssignedToUsers());
        assertEquals(1567L, summary.getCompletedAssignments());
        assertEquals(72.0, summary.getAvgProgress());
    }

    @Test
    void getSummary_clientAdminForcesScope() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext("ctx-client"));
        when(userTopicProgressReportRepository.aggregateSummaryMetrics(
                eq("ctx-client"), isNull(), nullable(Instant.class), nullable(Instant.class), isNull()))
                .thenReturn(TopicAssignmentSummaryDTO.builder()
                        .assignedTopics(0L).assignedToUsers(0L).completedAssignments(0L).avgProgress(0.0).build());
        when(subPackageRepositoryCustom.countDistinctTopicsInSubPackages(eq("ctx-client"), isNull()))
                .thenReturn(0L);

        service.getSummary(null, "other-client", null, null, null, null);

        verify(userTopicProgressReportRepository).aggregateSummaryMetrics(
                eq("ctx-client"), isNull(), nullable(Instant.class), nullable(Instant.class), isNull());
        verify(subPackageRepositoryCustom).countDistinctTopicsInSubPackages(eq("ctx-client"), isNull());
    }

    @Test
    void getSummary_emptyMspScopeReturnsZeros() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of()));

        TopicAssignmentSummaryDTO summary = service.getSummary(null, null, null, null, null, null);

        assertEquals(0L, summary.getTotalTopics());
        assertEquals(0L, summary.getAssignedToUsers());
        verify(userTopicProgressReportRepository, never()).aggregateSummaryMetrics(
                any(), any(), any(), any(), any());
    }

    @Test
    void getSummary_quickRangeOverridesExplicitDates() {
        when(userTopicProgressReportRepository.aggregateSummaryMetrics(
                isNull(), isNull(), any(Instant.class), any(Instant.class), isNull()))
                .thenReturn(TopicAssignmentSummaryDTO.builder()
                        .assignedTopics(1L).assignedToUsers(1L).completedAssignments(0L).avgProgress(10.0).build());
        when(subPackageRepositoryCustom.countDistinctTopicsInSubPackages(isNull(), isNull()))
                .thenReturn(1L);

        service.getSummary(null, null, null, "2020-01-01", "2020-01-02", QuickRange.LAST_7_DAYS);

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(userTopicProgressReportRepository).aggregateSummaryMetrics(
                isNull(), isNull(), startCaptor.capture(), endCaptor.capture(), isNull());

        LocalDate today = LocalDate.now();
        assertEquals(today.minusDays(7).toString(),
                startCaptor.getValue().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString());
        assertEquals(today.toString(),
                endCaptor.getValue().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString());
    }

    @Test
    void getTopicProgressList_passesStatusFilter_summaryDoesNotUseStatus() {
        when(userTopicProgressReportRepository.aggregateTopicProgressPage(
                isNull(), isNull(), nullable(Instant.class), nullable(Instant.class),
                eq(List.of("COMPLETED")), isNull(), eq(0), eq(20)))
                .thenReturn(List.of(TopicAssignmentProgressRowDTO.builder()
                        .topicId("t1").topicName("Phishing").assignedCount(10L).completedCount(10L)
                        .avgProgress(100.0).completionRate(100.0).build()));

        List<TopicAssignmentProgressRowDTO> rows = service.getTopicProgressList(
                null, null, null, List.of("COMPLETED"), null, null, null, 0, 20);

        assertEquals(1, rows.size());
        verify(userTopicProgressReportRepository).aggregateTopicProgressPage(
                isNull(), isNull(), nullable(Instant.class), nullable(Instant.class),
                eq(List.of("COMPLETED")), isNull(), eq(0), eq(20));
    }

    @Test
    void getSummary_doesNotPassStatusToRepository() {
        when(userTopicProgressReportRepository.aggregateSummaryMetrics(
                isNull(), isNull(), nullable(Instant.class), nullable(Instant.class), isNull()))
                .thenReturn(TopicAssignmentSummaryDTO.builder()
                        .assignedTopics(0L).assignedToUsers(0L).completedAssignments(0L).avgProgress(0.0).build());
        when(subPackageRepositoryCustom.countDistinctTopicsInSubPackages(isNull(), isNull()))
                .thenReturn(0L);

        service.getSummary(null, null, null, null, null, null);

        verify(userTopicProgressReportRepository).aggregateSummaryMetrics(
                isNull(), isNull(), nullable(Instant.class), nullable(Instant.class), isNull());
    }

    @Test
    void exportCsv_includesHeaderAndRows() {
        when(userTopicProgressReportRepository.aggregateTopicProgressForExport(
                isNull(), isNull(), nullable(Instant.class), nullable(Instant.class), isNull(), isNull()))
                .thenReturn(List.of(TopicAssignmentProgressRowDTO.builder()
                        .topicId("t1")
                        .topicName("Cybersecurity Fundamentals")
                        .assignedCount(450L)
                        .completedCount(380L)
                        .avgProgress(84.4)
                        .completionRate(84.44)
                        .build()));

        byte[] csv = service.exportCsv(null, null, null, null, null, null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.startsWith("\uFEFF"));
        assertTrue(content.contains("Topic ID,Topic Name,Assigned Count,Completed Count,Avg Progress %,Completion Rate %"));
        assertTrue(content.contains("Cybersecurity Fundamentals"));
        assertTrue(content.contains("450"));
        assertTrue(content.contains("380"));
    }

    @Test
    void mspScopeUsesClientAdminIds() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(mspContext(List.of("client-1", "client-2")));
        when(userTopicProgressReportRepository.aggregateSummaryMetrics(
                isNull(), eq(List.of("client-1", "client-2")), nullable(Instant.class), nullable(Instant.class), isNull()))
                .thenReturn(TopicAssignmentSummaryDTO.builder()
                        .assignedTopics(2L).assignedToUsers(5L).completedAssignments(1L).avgProgress(50.0).build());
        when(subPackageRepositoryCustom.countDistinctTopicsInSubPackages(
                isNull(), eq(List.of("client-1", "client-2"))))
                .thenReturn(3L);

        TopicAssignmentSummaryDTO summary = service.getSummary(null, null, null, null, null, null);

        assertEquals(3L, summary.getTotalTopics());
        assertEquals(1L, summary.getUnassignedTopics());
        verify(userTopicProgressReportRepository).aggregateSummaryMetrics(
                isNull(), eq(List.of("client-1", "client-2")), nullable(Instant.class), nullable(Instant.class), isNull());
    }

    private static CurrentUserContext aspireAdminContext() {
        return CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.ASPIRE_ADMIN.name())
                .build();
    }

    private static CurrentUserContext clientAdminContext(String clientAdminId) {
        return CurrentUserContext.builder()
                .userId(clientAdminId)
                .clientAdminId(clientAdminId)
                .userType(UserType.CLIENT_ADMIN.name())
                .build();
    }

    private static CurrentUserContext mspContext(List<String> clientAdminIds) {
        return CurrentUserContext.builder()
                .userId("msp-1")
                .userType(UserType.MSP.name())
                .clientAdminIds(clientAdminIds)
                .build();
    }
}
