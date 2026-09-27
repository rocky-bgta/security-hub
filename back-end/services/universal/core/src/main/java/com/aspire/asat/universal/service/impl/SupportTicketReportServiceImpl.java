package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.service.SupportTicketReportService;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.response.SupportTicketRecentRowDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketReportSummaryDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketStatusDistributionDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.utils.UserTypeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketReportServiceImpl implements SupportTicketReportService {
    private static final int DEFAULT_REPORT_DAYS = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketTypeRepository supportTicketTypeRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public SupportTicketReportSummaryDto getOpenVsClosedSummary(
            String clientAdminId, Instant fromDate, Instant toDate, String search, Integer thresholdDays) {
        ClientScope scope = resolveClientScope(clientAdminId);
        if (scope.isEmpty()) {
            return emptySummary();
        }

        Instant effectiveFromDate = fromDate != null ? fromDate : Instant.now().minus(DEFAULT_REPORT_DAYS, ChronoUnit.DAYS);
        Instant effectiveToDate = toDate != null ? toDate : Instant.now();

        Map<String, Long> metrics = fetchReportMetrics(scope, search, effectiveFromDate, effectiveToDate);

        return SupportTicketReportSummaryDto.builder()
                .totalTickets(metrics.getOrDefault("totalTickets", 0L))
                .openTickets(metrics.getOrDefault("openTickets", 0L))
                .closedTickets(metrics.getOrDefault("closedTickets", 0L))
                .highPriorityTickets(metrics.getOrDefault("highPriorityTickets", 0L))
                .build();
    }

    @Override
    public List<SupportTicketStatusDistributionDto> getOpenVsClosedStatusDistribution(
            String clientAdminId, Instant fromDate, Instant toDate, String search, Integer thresholdDays) {
        ClientScope scope = resolveClientScope(clientAdminId);
        if (scope.isEmpty()) {
            return emptyStatusDistribution();
        }

        Instant effectiveFromDate = fromDate != null ? fromDate : Instant.now().minus(DEFAULT_REPORT_DAYS, ChronoUnit.DAYS);
        Instant effectiveToDate = toDate != null ? toDate : Instant.now();

        Map<String, Long> metrics = fetchReportMetrics(scope, search, effectiveFromDate, effectiveToDate);

        return List.of(
                SupportTicketStatusDistributionDto.builder()
                        .status(TicketStatus.OPEN.name())
                        .count(metrics.getOrDefault("openTickets", 0L))
                        .build(),
                SupportTicketStatusDistributionDto.builder()
                        .status(TicketStatus.IN_PROGRESS.name())
                        .count(metrics.getOrDefault("inProgressTickets", 0L))
                        .build(),
                SupportTicketStatusDistributionDto.builder()
                        .status(TicketStatus.CLOSED.name())
                        .count(metrics.getOrDefault("closedTickets", 0L))
                        .build()
        );
    }

    @Override
    public AllResponseDto<List<SupportTicketRecentRowDto>> getOpenVsClosedRecentTickets(
            String clientAdminId, Instant fromDate, Instant toDate, String search, Integer thresholdDays, int offset, int pageSize) {
        ClientScope scope = resolveClientScope(clientAdminId);
        if (scope.isEmpty()) {
            return new AllResponseDto<>(offset, pageSize, 0L, List.of());
        }

        Instant effectiveFromDate = fromDate != null ? fromDate : Instant.now().minus(DEFAULT_REPORT_DAYS, ChronoUnit.DAYS);
        Instant effectiveToDate = toDate != null ? toDate : Instant.now();

        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<SupportTicket> page = fetchRecentTickets(scope, search, effectiveFromDate, effectiveToDate, pageable);
        Map<String, String> supportTypeNames = resolveSupportTypeNames(page.getContent());

        List<SupportTicketRecentRowDto> rows = page.getContent().stream()
                .map(ticket -> SupportTicketRecentRowDto.builder()
                        .ticketId(ticket.getTicketId())
                        .subject(ticket.getTitle())
                        .user(ticket.getUsername())
                        .priority(ticket.getPriority())
                        .status(ticket.getStatus())
                        .createdDate(ticket.getCreatedDate())
                        .category(resolveSupportTypeName(ticket.getSupportType(), supportTypeNames))
                        .build())
                .toList();

        return new AllResponseDto<>(offset, pageSize, page.getTotalElements(), rows);
    }

    @Override
    public byte[] exportOpenVsClosedReport(
            String clientAdminId, Instant fromDate, Instant toDate, String search, Integer thresholdDays) {
        ClientScope scope = resolveClientScope(clientAdminId);
        if (scope.isEmpty()) {
            return "Ticket ID,Subject,User,Priority,Status,Created Date,Category\n"
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        Instant effectiveFromDate = fromDate != null ? fromDate : Instant.now().minus(DEFAULT_REPORT_DAYS, ChronoUnit.DAYS);
        Instant effectiveToDate = toDate != null ? toDate : Instant.now();

        List<SupportTicket> tickets = fetchTicketsForExport(scope, search, effectiveFromDate, effectiveToDate);
        Map<String, String> supportTypeNames = resolveSupportTypeNames(tickets);

        StringBuilder csv = new StringBuilder();
        csv.append("Ticket ID,Subject,User,Priority,Status,Created Date,Category\n");
        for (SupportTicket ticket : tickets) {
            csv.append(escapeCsv(ticket.getTicketId())).append(",")
                    .append(escapeCsv(ticket.getTitle())).append(",")
                    .append(escapeCsv(ticket.getUsername())).append(",")
                    .append(escapeCsv(ticket.getPriority() != null ? ticket.getPriority().name() : "")).append(",")
                    .append(escapeCsv(ticket.getStatus() != null ? ticket.getStatus().name() : "")).append(",")
                    .append(escapeCsv(ticket.getCreatedDate() != null ? DATE_FORMATTER.format(ticket.getCreatedDate()) : "")).append(",")
                    .append(escapeCsv(resolveSupportTypeName(ticket.getSupportType(), supportTypeNames)))
                    .append("\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private Map<String, Long> fetchReportMetrics(ClientScope scope, String search, Instant fromDate, Instant toDate) {
        if (scope.isMulti()) {
            return supportTicketRepository.aggregateReportMetrics(scope.clientIds(), search, fromDate, toDate);
        }
        return supportTicketRepository.aggregateReportMetrics(scope.clientId(), search, fromDate, toDate);
    }

    private Page<SupportTicket> fetchRecentTickets(ClientScope scope, String search, Instant fromDate, Instant toDate, Pageable pageable) {
        if (scope.isMulti()) {
            return supportTicketRepository.findSupportTicketsWithFilters(
                    scope.clientIds(), null, null, null, null, null, null, null, null, null, null, search,
                    fromDate, toDate, pageable);
        }
        return supportTicketRepository.findSupportTicketsWithFilters(
                scope.clientId(), null, null, null, null, null, null, null, null, null, null, search,
                fromDate, toDate, pageable);
    }

    private List<SupportTicket> fetchTicketsForExport(ClientScope scope, String search, Instant fromDate, Instant toDate) {
        if (scope.isMulti()) {
            return supportTicketRepository.findSupportTicketsForReportExport(scope.clientIds(), search, fromDate, toDate);
        }
        return supportTicketRepository.findSupportTicketsForReportExport(scope.clientId(), search, fromDate, toDate);
    }

    private Map<String, String> resolveSupportTypeNames(List<SupportTicket> tickets) {
        Set<String> supportTypeIds = tickets.stream()
                .map(SupportTicket::getSupportType)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (supportTypeIds.isEmpty()) {
            return Map.of();
        }

        return supportTicketTypeRepository.findAllById(supportTypeIds).stream()
                .filter(type -> type.getName() != null)
                .collect(Collectors.toMap(SupportTicketType::getId, SupportTicketType::getName, (left, right) -> left));
    }

    private String resolveSupportTypeName(String supportTypeId, Map<String, String> supportTypeNames) {
        if (!StringUtils.hasText(supportTypeId)) {
            return null;
        }
        return supportTypeNames.get(supportTypeId);
    }

    private SupportTicketReportSummaryDto emptySummary() {
        return SupportTicketReportSummaryDto.builder()
                .totalTickets(0L)
                .openTickets(0L)
                .closedTickets(0L)
                .highPriorityTickets(0L)
                .build();
    }

    private List<SupportTicketStatusDistributionDto> emptyStatusDistribution() {
        return List.of(
                SupportTicketStatusDistributionDto.builder().status(TicketStatus.OPEN.name()).count(0L).build(),
                SupportTicketStatusDistributionDto.builder().status(TicketStatus.IN_PROGRESS.name()).count(0L).build(),
                SupportTicketStatusDistributionDto.builder().status(TicketStatus.CLOSED.name()).count(0L).build()
        );
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private ClientScope resolveClientScope(String requestedClientAdminId) {
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();

        if (UserTypeUtils.isClientAdmin(ctx.getUserType())) {
            return ClientScope.single(ctx.getClientAdminId());
        }

        if (isMspReportRequest(ctx)) {
            List<String> clientIds = resolveMspClientAdminIds(ctx, requestedClientAdminId);
            if (clientIds.isEmpty()) {
                return ClientScope.empty();
            }
            if (clientIds.size() == 1) {
                return ClientScope.single(clientIds.get(0));
            }
            return ClientScope.multi(clientIds);
        }

        return ClientScope.single(requestedClientAdminId);
    }

    private List<String> resolveMspClientAdminIds(CurrentUserContext context, String clientAdminId) {
        String normalizedClientAdminId = (clientAdminId != null && !clientAdminId.isBlank()) ? clientAdminId.trim() : null;
        if (normalizedClientAdminId != null) {
            return List.of(normalizedClientAdminId);
        }

        List<String> contextClientAdminIds = context.getClientAdminIds();
        if (contextClientAdminIds != null && !contextClientAdminIds.isEmpty()) {
            return contextClientAdminIds;
        }

        return List.of();
    }

    private boolean isMspReportRequest(CurrentUserContext context) {
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private record ClientScope(String clientId, List<String> clientIds) {
        static ClientScope single(String clientId) {
            return new ClientScope(clientId, null);
        }

        static ClientScope multi(List<String> clientIds) {
            return new ClientScope(null, clientIds);
        }

        static ClientScope empty() {
            return new ClientScope(null, Collections.emptyList());
        }

        boolean isMulti() {
            return clientIds != null && clientIds.size() > 1;
        }

        boolean isEmpty() {
            return clientIds != null && clientIds.isEmpty();
        }
    }
}
