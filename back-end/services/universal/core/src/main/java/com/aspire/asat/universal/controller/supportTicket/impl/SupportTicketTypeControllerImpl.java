package com.aspire.asat.universal.controller.supportTicket.impl;

import com.aspire.asat.universal.controller.supportTicket.SupportTicketTypeController;
import com.aspire.asat.universal.service.SupportTicketTypeService;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeRequestDTO;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeResponseDTO;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SupportTicketTypeControllerImpl implements SupportTicketTypeController {

    private final SupportTicketTypeService supportTicketTypeService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SupportTicketTypeResponseDTO>>>> getAllSupportTicketTypes(
            String search, Boolean active, int offset, int pageSize, String sortBy, String sortDirection) {
        log.info("REST request to get all support ticket types");
        AllResponseDto<List<SupportTicketTypeResponseDTO>> response = supportTicketTypeService
                .getAllSupportTicketTypes(search, active, offset, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket types retrieved successfully", 200, response));
    }


    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketTypeResponseDTO>> getSupportTicketTypeById(String id) {
        log.info("REST request to get support ticket type by id: {}", id);
        SupportTicketTypeResponseDTO response = supportTicketTypeService.getSupportTicketTypeById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket type retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketTypeResponseDTO>> createSupportTicketType(SupportTicketTypeRequestDTO request) {
        log.info("REST request to create support ticket type");
        SupportTicketTypeResponseDTO response = supportTicketTypeService.createSupportTicketType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Support ticket type created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteSupportTicketType(String id) {
        log.info("REST request to delete support ticket type with id: {}", id);
        supportTicketTypeService.deleteSupportTicketType(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket type deleted successfully", 200, ""));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketTypeResponseDTO>> updateSupportTicketType(String id, SupportTicketTypeRequestDTO request) {
        log.info("REST request to update support ticket type with id: {}", id);
        SupportTicketTypeResponseDTO response = supportTicketTypeService.updateSupportTicketType(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket type updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<SupportTicketTypeResponseDTO>>> getAllActiveSupportTicketTypes() {
        log.info("REST request to get all active support ticket types");
        List<SupportTicketTypeResponseDTO> response = supportTicketTypeService.getAllActiveSupportTicketTypes();
        return ResponseEntity.ok(new ApiResponseDto<>("Active support ticket types retrieved successfully", 200, response));
    }

}
