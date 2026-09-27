package com.aspire.asat.universal.service;

import com.aspire.asat.universal.supportTicket.SupportTicketTypeRequestDTO;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeResponseDTO;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;

import java.util.List;

public interface SupportTicketTypeService {

    AllResponseDto<List<SupportTicketTypeResponseDTO>> getAllSupportTicketTypes(String search, Boolean active,
                                                                                 int offset, int pageSize,
                                                                                 String sortBy, String sortDirection);

    SupportTicketTypeResponseDTO getSupportTicketTypeById(String id);

    SupportTicketTypeResponseDTO createSupportTicketType(SupportTicketTypeRequestDTO request);

    void deleteSupportTicketType(String id);
    SupportTicketTypeResponseDTO updateSupportTicketType(String id, SupportTicketTypeRequestDTO request);

    List<SupportTicketTypeResponseDTO> getAllActiveSupportTicketTypes();
}
