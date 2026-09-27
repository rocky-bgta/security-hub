package com.aspire.asat.universal.repository.custom;

import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Custom repository interface for SupportTicket with complex query methods
 */
public interface SupportTicketRepositoryCustom {

    Page<SupportTicket> findSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            Pageable pageable
    );

    Page<SupportTicket> findSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate,
            Pageable pageable
    );

    long countSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search
    );

    long countSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate
    );

    /**
     * Count tickets created on a specific date (based on createdDate)
     * Used to generate sequential ticket IDs
     */
    long countByCreatedDateBetween(java.time.Instant startOfDay, java.time.Instant startOfNextDay);

    Map<String, Long> aggregateReportMetrics(
            String clientId,
            String search,
            Instant fromDate,
            Instant toDate
    );

    Map<String, Long> aggregateReportMetrics(
            List<String> clientIds,
            String search,
            Instant fromDate,
            Instant toDate
    );

    List<SupportTicket> findSupportTicketsForReportExport(
            String clientId,
            String search,
            Instant fromDate,
            Instant toDate
    );

    List<SupportTicket> findSupportTicketsForReportExport(
            List<String> clientIds,
            String search,
            Instant fromDate,
            Instant toDate
    );

    List<SupportResolutionTimeTypeAggregate> aggregateResolutionTimeBySupportType(String clientId);

    Page<SupportTicket> findSupportTicketsWithFilters(
            List<String> clientIds,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );
}

