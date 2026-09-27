package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.repository.custom.SupportResolutionTimeTypeAggregate;
import com.aspire.asat.universal.supportTicket.response.SupportResolutionTimeResponseDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SupportResolutionTimeServiceImplTest {

    private static final String SUPPORT_TYPE_ID = "type-billing-1";
    private static final String CLIENT_ADMIN_USER_ID = "client-admin-user-1";

    @Mock
    private SupportTicketRepository supportTicketRepository;
    @Mock
    private SupportTicketTypeRepository supportTicketTypeRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private SupportResolutionTimeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder()
                        .userId(CLIENT_ADMIN_USER_ID)
                        .userType(UserType.CLIENT_ADMIN.name())
                        .build());
    }

    @Test
    void resolveClientId_usesLoggedInUserIdForClientAdmin() {
        assertEquals(CLIENT_ADMIN_USER_ID, service.resolveClientId(null));
        assertEquals(CLIENT_ADMIN_USER_ID, service.resolveClientId("ignored-param"));
    }

    @Test
    void getSupportResolutionTime_calculatesAverageAndSlaCompliance() {
        SupportResolutionTimeTypeAggregate aggregate = SupportResolutionTimeTypeAggregate.builder()
                .supportTypeId(SUPPORT_TYPE_ID)
                .totalTickets(4)
                .openTickets(1)
                .inProgressTickets(1)
                .closedTickets(2)
                .totalClosedResolutionMs(36_000_000L) // 10 hours
                .build();

        when(supportTicketRepository.aggregateResolutionTimeBySupportType(eq(CLIENT_ADMIN_USER_ID)))
                .thenReturn(List.of(aggregate));
        when(supportTicketTypeRepository.findAllById(Set.of(SUPPORT_TYPE_ID)))
                .thenReturn(List.of(new SupportTicketType(
                        SUPPORT_TYPE_ID, "Billing Issue", null, null, null, null, null, true)));

        SupportResolutionTimeResponseDto response = service.getSupportResolutionTime(null);

        assertEquals(5.0, response.getAverageResolutionTime());
        assertEquals(50.0, response.getSlaCompliance());
        assertEquals(4, response.getTotalTickets());
        assertEquals(2, response.getClosedTickets());
        assertEquals(2, response.getOpenedTickets());
        assertEquals("Billing Issue", response.getBySupportType().get(0).getSupportTypeName());
    }

    @Test
    void exportSupportResolutionTimeCsv_containsByTypeColumns() {
        SupportResolutionTimeTypeAggregate aggregate = SupportResolutionTimeTypeAggregate.builder()
                .supportTypeId(SUPPORT_TYPE_ID)
                .totalTickets(4)
                .openTickets(1)
                .inProgressTickets(1)
                .closedTickets(2)
                .totalClosedResolutionMs(36_000_000L)
                .build();

        when(supportTicketRepository.aggregateResolutionTimeBySupportType(eq(CLIENT_ADMIN_USER_ID)))
                .thenReturn(List.of(aggregate));
        when(supportTicketTypeRepository.findAllById(Set.of(SUPPORT_TYPE_ID)))
                .thenReturn(List.of(new SupportTicketType(
                        SUPPORT_TYPE_ID, "Billing Issue", null, null, null, null, null, true)));

        byte[] csv = service.exportSupportResolutionTimeCsv(null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.contains(
                "Support Ticket Type,Average Resolution Time (Hours),Total Tickets,Open Tickets,"
                        + "Closed Tickets,In Progress Tickets,SLA Compliance (%)"));
        assertTrue(content.contains("Billing Issue,5.0,4,1,2,1,50.0"));
    }
}
