package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.universal.client.UniversalNotificationClient;
import com.aspire.asat.universal.data.externalresponses.UserDetailsDto;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.exception.ResourceNotFoundException;
import com.aspire.asat.universal.exception.UniversalServiceException;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.service.ExternalApiService;
import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.request.SupportTicketCreateRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketUpdateRequestDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketGetResponseDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceImplTest {

    private static final String TICKET_ID = "ticket-1";
    private static final String MSP_ID = "msp-1";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String END_USER_ID = "end-user-1";
    private static final String SUPPORT_TYPE_ID = "support-type-1";

    @Mock
    private SupportTicketRepository supportTicketRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ExternalApiService externalApiService;
    @Mock
    private SupportTicketTypeRepository supportTicketTypeRepository;
    @Mock
    private UniversalNotificationClient universalNotificationClient;

    @InjectMocks
    private SupportTicketServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(supportTicketRepository.countByCreatedDateBetween(any(), any())).thenReturn(0L);
        lenient().when(supportTicketRepository.findByTicketId(any())).thenReturn(Optional.empty());
        lenient().when(supportTicketRepository.save(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getAllSupportTickets_mspUser_overridesMspIdWithCurrentUserId() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext());
        when(supportTicketRepository.findSupportTicketsWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(MSP_ID), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyTicketPage());

        service.getAllSupportTickets(null, null, null, null, null, "other-msp", null, null, null, null, 0, 10);

        verify(supportTicketRepository).findSupportTicketsWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(MSP_ID), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getAllSupportTickets_nonMspUser_usesPassedMspId() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());
        when(supportTicketRepository.findSupportTicketsWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("requested-msp"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyTicketPage());

        service.getAllSupportTickets(null, null, null, null, null, "requested-msp", null, null, null, null, 0, 10);

        verify(supportTicketRepository).findSupportTicketsWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("requested-msp"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getAllSupportTickets_returnsMappedTicketsWithSupportTypeName() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());
        SupportTicket ticket = sampleTicket();
        when(supportTicketRepository.findSupportTicketsWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket)));
        when(supportTicketTypeRepository.findById(SUPPORT_TYPE_ID))
                .thenReturn(Optional.of(new SupportTicketType(SUPPORT_TYPE_ID, "Dashboard Related", null, null, null, null, null, true)));

        AllResponseDto<List<SupportTicketResponseDto>> response = service.getAllSupportTickets(
                CLIENT_ADMIN_ID, null, TicketStatus.OPEN, null, null, null, null, null, null, null, 0, 10);

        assertEquals(1, response.getItems().size());
        assertEquals("Dashboard Related", response.getItems().get(0).getSupportType());
        assertEquals("TKT-20260626-001", response.getItems().get(0).getTicketId());
    }

    @Test
    void getAllSupportTickets_repositoryFailure_throwsUniversalServiceException() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());
        when(supportTicketRepository.findSupportTicketsWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("db error"));

        UniversalServiceException exception = assertThrows(UniversalServiceException.class,
                () -> service.getAllSupportTickets(null, null, null, null, null, null, null, null, null, null, 0, 10));

        assertTrue(exception.getMessage().contains("Failed to fetch support tickets"));
    }

    @Test
    void getSupportTicketById_found_returnsTicketWithSupportType() {
        SupportTicket ticket = sampleTicket();
        ticket.setId(TICKET_ID);
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(supportTicketTypeRepository.findById(SUPPORT_TYPE_ID))
                .thenReturn(Optional.of(new SupportTicketType(SUPPORT_TYPE_ID, "Technical Error", null, null, null, null, null, true)));

        SupportTicketGetResponseDto response = service.getSupportTicketById(TICKET_ID);

        assertEquals(TICKET_ID, response.getId());
        assertNotNull(response.getSupportType());
        assertEquals("Technical Error", response.getSupportType().getName());
    }

    @Test
    void getSupportTicketById_notFound_throwsResourceNotFoundException() {
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSupportTicketById(TICKET_ID));
    }

    @Test
    void updateTicketStatus_success() {
        SupportTicket ticket = sampleTicket();
        ticket.setId(TICKET_ID);
        ticket.setUserId(END_USER_ID);
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());
        when(externalApiService.getEndUserDetails(END_USER_ID))
                .thenReturn(Optional.of(new UserDetailsDto(END_USER_ID, "John", "Doe", "John Doe", "john@example.com", CLIENT_ADMIN_ID)));

        SupportTicketResponseDto response = service.updateTicketStatus(TICKET_ID, TicketStatus.CLOSED, CLIENT_ADMIN_ID);

        assertEquals(TicketStatus.CLOSED, response.getStatus());
        verify(supportTicketRepository).save(ticket);
        verify(universalNotificationClient).sendTicketStatusUpdateNotification(
                eq("john@example.com"), eq(END_USER_ID), eq("John Doe"), eq("TKT-20260626-001"),
                eq(TicketStatus.CLOSED), eq(CLIENT_ADMIN_ID));
    }

    @Test
    void updateTicketStatus_notFound_throwsResourceNotFoundException() {
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateTicketStatus(TICKET_ID, TicketStatus.CLOSED, CLIENT_ADMIN_ID));
    }

    @Test
    void deleteSupportTicket_success() {
        SupportTicket ticket = sampleTicket();
        ticket.setId(TICKET_ID);
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

        service.deleteSupportTicket(TICKET_ID);

        verify(supportTicketRepository).delete(ticket);
    }

    @Test
    void deleteSupportTicket_notFound_throwsResourceNotFoundException() {
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteSupportTicket(TICKET_ID));
    }

    @Test
    void createSupportTicket_endUser_assignsToClientAdmin() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(endUserContext());

        SupportTicketResponseDto response = service.createSupportTicket(createRequest());

        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals(CLIENT_ADMIN_ID, response.getClientId());
        assertEquals(CLIENT_ADMIN_ID, response.getAssignedTo());
        assertEquals(AssignCategory.ASSIGNTOCLIENT, response.getAssignCategory());
        assertNull(response.getMspId());
        verify(universalNotificationClient).sendTicketCreatedNotification(
                eq("user@example.com"), eq(CLIENT_ADMIN_ID), eq("Client Admin"), any(), eq(CLIENT_ADMIN_ID));
    }

    @Test
    void createSupportTicket_clientAdmin_assignsToMsp() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());
        when(externalApiService.getMspIdByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(Optional.of(MSP_ID));

        SupportTicketResponseDto response = service.createSupportTicket(createRequest());

        assertEquals(CLIENT_ADMIN_ID, response.getClientId());
        assertEquals(MSP_ID, response.getAssignedTo());
        assertEquals(MSP_ID, response.getMspId());
        assertEquals(AssignCategory.ASSIGNTOMSP, response.getAssignCategory());
    }

    @Test
    void createSupportTicket_mspAdmin_assignsToSuperAdmin() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext());

        SupportTicketResponseDto response = service.createSupportTicket(createRequest());

        assertEquals(MSP_ID, response.getMspId());
        assertTrue(response.getAssignToSuperAdmin());
        assertEquals(AssignCategory.ASSIGNTOSUPER, response.getAssignCategory());
        assertNull(response.getClientId());
    }

    @Test
    void createSupportTicket_unsupportedUserType_throwsException() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder()
                        .userId("admin-1")
                        .userType(UserType.ASPIRE_ADMIN.name())
                        .build());

        UniversalServiceException exception = assertThrows(UniversalServiceException.class,
                () -> service.createSupportTicket(createRequest()));

        assertTrue(exception.getMessage().contains("Unsupported user type"));
    }

    @Test
    void updateSupportTicket_success() {
        SupportTicket ticket = sampleTicket();
        ticket.setId(TICKET_ID);
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

        SupportTicketUpdateRequestDto updateRequest = SupportTicketUpdateRequestDto.builder()
                .title("Updated title")
                .status(TicketStatus.IN_PROGRESS)
                .updatedBy(CLIENT_ADMIN_ID)
                .build();

        SupportTicketResponseDto response = service.updateSupportTicket(TICKET_ID, updateRequest);

        assertEquals("Updated title", response.getTitle());
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
        verify(supportTicketRepository).save(ticket);
    }

    @Test
    void updateSupportTicket_notFound_throwsResourceNotFoundException() {
        when(supportTicketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateSupportTicket(TICKET_ID, SupportTicketUpdateRequestDto.builder().title("Updated").build()));
    }

    @Test
    void createSupportTicket_clientAdmin_usesStaticMspIdWhenLookupFails() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());
        when(externalApiService.getMspIdByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        ArgumentCaptor<SupportTicket> ticketCaptor = ArgumentCaptor.forClass(SupportTicket.class);
        when(supportTicketRepository.save(ticketCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createSupportTicket(createRequest());

        assertEquals("msp-admin-default", ticketCaptor.getValue().getMspId());
        assertEquals("msp-admin-default", ticketCaptor.getValue().getAssignedTo());
    }

    private static Page<SupportTicket> emptyTicketPage() {
        return new PageImpl<>(List.of());
    }

    private static SupportTicket sampleTicket() {
        return SupportTicket.builder()
                .ticketId("TKT-20260626-001")
                .title("Password Issue")
                .username("client.admin@yopmail.com")
                .clientId(CLIENT_ADMIN_ID)
                .priority(Priority.HIGH)
                .status(TicketStatus.OPEN)
                .supportType(SUPPORT_TYPE_ID)
                .createdDate(Instant.parse("2026-06-26T15:42:39.612Z"))
                .build();
    }

    private static SupportTicketCreateRequestDto createRequest() {
        return SupportTicketCreateRequestDto.builder()
                .title("Password Issue")
                .priority(Priority.HIGH)
                .supportType(SUPPORT_TYPE_ID)
                .description("Unable to reset password for user account")
                .build();
    }

    private static CurrentUserContext endUserContext() {
        return CurrentUserContext.builder()
                .userId(END_USER_ID)
                .username("enduser")
                .userType("END_USER")
                .email("user@example.com")
                .clientAdminId(CLIENT_ADMIN_ID)
                .clientAdminEmail("admin@example.com")
                .clientAdminFullName("Client Admin")
                .build();
    }

    private static CurrentUserContext clientAdminContext() {
        return CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .username("client.admin@yopmail.com")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
    }

    private static CurrentUserContext mspContext() {
        return CurrentUserContext.builder()
                .userId(MSP_ID)
                .username("msp.admin@yopmail.com")
                .userType(UserType.MSP.getValue())
                .build();
    }
}
