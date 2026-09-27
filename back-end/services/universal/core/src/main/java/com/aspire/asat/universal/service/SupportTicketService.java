package com.aspire.asat.universal.service;


import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.request.SupportTicketCreateRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketUpdateRequestDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketGetResponseDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;

import java.util.List;

public interface SupportTicketService {

    /**
     * Create a new support ticket
     *
     * @param requestDto The request DTO containing ticket details
     * @return Created support ticket response
     */
    SupportTicketResponseDto createSupportTicket(SupportTicketCreateRequestDto requestDto);

    /**
     * Get a support ticket by ID
     *
     * @param id The ticket ID
     * @return Support ticket response
     */
    SupportTicketGetResponseDto getSupportTicketById(String id);

    /**
     * Update a support ticket
     *
     * @param id The ticket ID
     * @param requestDto The request DTO containing updated ticket details
     * @return Updated support ticket response
     */
    SupportTicketResponseDto updateSupportTicket(String id, SupportTicketUpdateRequestDto requestDto);

    /**
     * Delete a support ticket
     *
     * @param id The ticket ID
     */
    void deleteSupportTicket(String id);

    /**
     * Get all support tickets with pagination and filters
     *
     * @param clientId Filter by client ID
     * @param assignedTo Filter by assigned staff
     * @param status Filter by status
     * @param priority Filter by priority
     * @param supportType Filter by support type
     * @param mspId Filter by MSP ID
     * @param assignToSuperAdmin Filter by assignToSuperAdmin flag
     * @param createdBy Filter by createdBy (user ID who created the ticket)
     * @param assignCategory Filter by assignCategory (ASSIGNTOCLIENT, ASSIGNTOMSP, ASSIGNTOSUPER)
     * @param search Search term for title/description
     * @param offset Page offset
     * @param pageSize Page size
     * @return Paginated list of support tickets
     */
    AllResponseDto<List<SupportTicketResponseDto>> getAllSupportTickets(
            String clientId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            int offset,
            int pageSize
    );


    /**
     * Update ticket status
     *
     * @param id The ticket ID
     * @param status The new status
     * @param updatedBy The user updating the status
     * @return Updated support ticket response
     */
    SupportTicketResponseDto updateTicketStatus(String id, TicketStatus status, String updatedBy);
}

