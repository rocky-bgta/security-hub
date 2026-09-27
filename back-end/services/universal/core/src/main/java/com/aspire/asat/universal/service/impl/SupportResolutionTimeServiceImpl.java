package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.repository.custom.SupportResolutionTimeTypeAggregate;
import com.aspire.asat.universal.service.SupportResolutionTimeService;
import com.aspire.asat.universal.supportTicket.response.SupportResolutionTimeByTypeDto;
import com.aspire.asat.universal.supportTicket.response.SupportResolutionTimeResponseDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.utils.UserTypeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportResolutionTimeServiceImpl implements SupportResolutionTimeService {

    private static final double MILLIS_PER_HOUR = 3_600_000.0;

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketTypeRepository supportTicketTypeRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public SupportResolutionTimeResponseDto getSupportResolutionTime(String clientAdminId) {
        String clientId = resolveClientId(clientAdminId);
        if (!StringUtils.hasText(clientId)) {
            return emptyResponse();
        }

        List<SupportResolutionTimeTypeAggregate> aggregates =
                supportTicketRepository.aggregateResolutionTimeBySupportType(clientId);
        Map<String, String> supportTypeNames = resolveSupportTypeNames(aggregates);

        List<SupportResolutionTimeByTypeDto> bySupportType = aggregates.stream()
                .map(aggregate -> toByTypeDto(aggregate, supportTypeNames))
                .toList();

        long totalTickets = aggregates.stream().mapToLong(SupportResolutionTimeTypeAggregate::getTotalTickets).sum();
        long closedTickets = aggregates.stream().mapToLong(SupportResolutionTimeTypeAggregate::getClosedTickets).sum();
        long openedTickets = totalTickets - closedTickets;
        long totalClosedResolutionMs = aggregates.stream()
                .mapToLong(SupportResolutionTimeTypeAggregate::getTotalClosedResolutionMs)
                .sum();

        return SupportResolutionTimeResponseDto.builder()
                .averageResolutionTime(averageHours(totalClosedResolutionMs, closedTickets))
                .slaCompliance(slaCompliance(closedTickets, totalTickets))
                .totalTickets(totalTickets)
                .closedTickets(closedTickets)
                .openedTickets(openedTickets)
                .bySupportType(bySupportType)
                .build();
    }

    @Override
    public byte[] exportSupportResolutionTimeCsv(String clientAdminId) {
        SupportResolutionTimeResponseDto report = getSupportResolutionTime(clientAdminId);
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("Support Ticket Type,Average Resolution Time (Hours),Total Tickets,Open Tickets,")
                .append("Closed Tickets,In Progress Tickets,SLA Compliance (%)\n");

        List<SupportResolutionTimeByTypeDto> rows = report.getBySupportType() != null
                ? report.getBySupportType()
                : List.of();

        for (SupportResolutionTimeByTypeDto row : rows) {
            csv.append(escapeCsv(row.getSupportTypeName())).append(',')
                    .append(row.getAverageResolutionTime()).append(',')
                    .append(row.getTotalTickets()).append(',')
                    .append(row.getOpenTickets()).append(',')
                    .append(row.getClosedTickets()).append(',')
                    .append(row.getInProgressTickets()).append(',')
                    .append(row.getSlaCompliance())
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * CLIENT_ADMIN tickets store clientId as the client admin userId.
     * For CLIENT_ADMIN users, always use the logged-in userId.
     * Otherwise use the clientAdminId query parameter.
     */
    String resolveClientId(String clientAdminId) {
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
        if (UserTypeUtils.isClientAdmin(ctx.getUserType())) {
            return ctx.getUserId();
        }
        return StringUtils.hasText(clientAdminId) ? clientAdminId.trim() : null;
    }

    private Map<String, String> resolveSupportTypeNames(List<SupportResolutionTimeTypeAggregate> aggregates) {
        Set<String> supportTypeIds = aggregates.stream()
                .map(SupportResolutionTimeTypeAggregate::getSupportTypeId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (supportTypeIds.isEmpty()) {
            return Map.of();
        }

        return supportTicketTypeRepository.findAllById(supportTypeIds).stream()
                .filter(type -> type.getName() != null)
                .collect(Collectors.toMap(SupportTicketType::getId, SupportTicketType::getName, (left, right) -> left));
    }

    private SupportResolutionTimeByTypeDto toByTypeDto(
            SupportResolutionTimeTypeAggregate aggregate,
            Map<String, String> supportTypeNames) {
        String supportTypeId = aggregate.getSupportTypeId();
        String supportTypeName = StringUtils.hasText(supportTypeId)
                ? supportTypeNames.getOrDefault(supportTypeId, supportTypeId)
                : "Unknown";

        return SupportResolutionTimeByTypeDto.builder()
                .supportTypeId(supportTypeId)
                .supportTypeName(supportTypeName)
                .averageResolutionTime(averageHours(
                        aggregate.getTotalClosedResolutionMs(), aggregate.getClosedTickets()))
                .totalTickets(aggregate.getTotalTickets())
                .openTickets(aggregate.getOpenTickets())
                .closedTickets(aggregate.getClosedTickets())
                .inProgressTickets(aggregate.getInProgressTickets())
                .slaCompliance(slaCompliance(aggregate.getClosedTickets(), aggregate.getTotalTickets()))
                .build();
    }

    private static double averageHours(long totalClosedResolutionMs, long closedTickets) {
        if (closedTickets <= 0) {
            return 0.0;
        }
        return roundTwoDecimals(totalClosedResolutionMs / MILLIS_PER_HOUR / closedTickets);
    }

    private static double slaCompliance(long closedTickets, long totalTickets) {
        if (totalTickets <= 0) {
            return 0.0;
        }
        return roundTwoDecimals((double) closedTickets / totalTickets * 100.0);
    }

    private static double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private SupportResolutionTimeResponseDto emptyResponse() {
        return SupportResolutionTimeResponseDto.builder()
                .averageResolutionTime(0.0)
                .slaCompliance(0.0)
                .totalTickets(0L)
                .closedTickets(0L)
                .openedTickets(0L)
                .bySupportType(List.of())
                .build();
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
}
