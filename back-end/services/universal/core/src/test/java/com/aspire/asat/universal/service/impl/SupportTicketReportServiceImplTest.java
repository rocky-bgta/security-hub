package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.response.SupportTicketRecentRowDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

class SupportTicketReportServiceImplTest {

    private static final String SUPPORT_TYPE_ID = "605ba680d75113f2ac03359";

    @Mock
    private SupportTicketRepository supportTicketRepository;
    @Mock
    private SupportTicketTypeRepository supportTicketTypeRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private SupportTicketReportServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder()
                        .userId("admin-1")
                        .userType(UserType.ASPIRE_ADMIN.name())
                        .build());
    }

    @Test
    void getOpenVsClosedRecentTickets_shouldReturnCategoryNameFromSupportTicketTypes() {
        SupportTicket ticket = sampleTicket();
        when(supportTicketRepository.findSupportTicketsWithFilters(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket)));
        when(supportTicketTypeRepository.findAllById(Set.of(SUPPORT_TYPE_ID)))
                .thenReturn(List.of(new SupportTicketType(SUPPORT_TYPE_ID, "Billing Issue", null, null, null, null, null, true)));

        AllResponseDto<List<SupportTicketRecentRowDto>> response = service.getOpenVsClosedRecentTickets(
                "client-1", null, null, null, null, 0, 10);

        assertEquals("Billing Issue", response.getItems().get(0).getCategory());
    }

    @Test
    void exportOpenVsClosedReport_shouldIncludeResolvedCategoryNameInCsv() {
        SupportTicket ticket = sampleTicket();
        when(supportTicketRepository.findSupportTicketsForReportExport(eq("client-1"), isNull(), any(), any()))
                .thenReturn(List.of(ticket));
        when(supportTicketTypeRepository.findAllById(Set.of(SUPPORT_TYPE_ID)))
                .thenReturn(List.of(new SupportTicketType(SUPPORT_TYPE_ID, "Billing Issue", null, null, null, null, null, true)));

        byte[] csv = service.exportOpenVsClosedReport("client-1", null, null, null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.contains("Billing Issue"));
        assertTrue(!content.contains(SUPPORT_TYPE_ID));
    }

    @Test
    void getOpenVsClosedRecentTickets_shouldReturnNullWhenSupportTypeNotFound() {
        SupportTicket ticket = sampleTicket();
        when(supportTicketRepository.findSupportTicketsWithFilters(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket)));
        when(supportTicketTypeRepository.findAllById(Set.of(SUPPORT_TYPE_ID)))
                .thenReturn(List.of());

        AllResponseDto<List<SupportTicketRecentRowDto>> response = service.getOpenVsClosedRecentTickets(
                "client-1", null, null, null, null, 0, 10);

        assertNull(response.getItems().get(0).getCategory());
    }

    private static SupportTicket sampleTicket() {
        return SupportTicket.builder()
                .ticketId("TKT-20260626-001")
                .title("Autem maxime volupta")
                .username("siddik@onystyle.com")
                .clientId("client-1")
                .priority(Priority.HIGH)
                .status(TicketStatus.OPEN)
                .supportType(SUPPORT_TYPE_ID)
                .createdDate(Instant.parse("2026-06-26T15:42:39.612Z"))
                .build();
    }
}
