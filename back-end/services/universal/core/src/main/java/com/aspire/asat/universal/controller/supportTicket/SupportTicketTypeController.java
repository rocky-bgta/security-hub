package com.aspire.asat.universal.controller.supportTicket;

import com.aspire.asat.universal.constant.WebApiUrlConstants;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeRequestDTO;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeResponseDTO;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Support Ticket Type", description = "Support Ticket Type Management APIs")
@RequestMapping(value = WebApiUrlConstants.API_URI_ROOT + "/support-ticket-types", produces = "application/json")
public interface SupportTicketTypeController {

    @Operation(summary = "Get all support ticket types")
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SupportTicketTypeResponseDTO>>>> getAllSupportTicketTypes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection);

    @Operation(summary = "Get support ticket type by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<SupportTicketTypeResponseDTO>> getSupportTicketTypeById(@PathVariable String id);

    @Operation(summary = "Create a new support ticket type")
    @PostMapping
    ResponseEntity<ApiResponseDto<SupportTicketTypeResponseDTO>> createSupportTicketType(
            @Valid @RequestBody SupportTicketTypeRequestDTO request);

    @Operation(summary = "Soft delete a support ticket type")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<String>> deleteSupportTicketType(@PathVariable String id);

    @Operation(summary = "Update a support ticket type")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<SupportTicketTypeResponseDTO>> updateSupportTicketType(
            @PathVariable String id,
            @Valid @RequestBody SupportTicketTypeRequestDTO request);

    @Operation(summary = "Get all active support ticket types without pagination")
    @GetMapping("/all")
    ResponseEntity<ApiResponseDto<List<SupportTicketTypeResponseDTO>>> getAllActiveSupportTicketTypes();

}
