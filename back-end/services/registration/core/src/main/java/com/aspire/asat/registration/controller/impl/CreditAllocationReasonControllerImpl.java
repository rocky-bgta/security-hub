package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.CreditAllocationReasonController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.creditAllocation.CreateCreditAllocationReasonRequestDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonDropdownDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonResponseDTO;
import com.aspire.asat.registration.data.creditAllocation.UpdateCreditAllocationReasonRequestDTO;
import com.aspire.asat.registration.service.CreditAllocationReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CreditAllocationReasonControllerImpl implements CreditAllocationReasonController {

    private final CreditAllocationReasonService service;

    @Override
    public ResponseEntity<ApiResponseDto<CreditAllocationReasonResponseDTO>> createReason(CreateCreditAllocationReasonRequestDTO dto) {
        CreditAllocationReasonResponseDTO response = service.createReason(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Credit allocation reason created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditAllocationReasonResponseDTO>>>> getAllReasons(
            String search,
            Boolean isActive,
            int offset,
            int limit
    ) {
        List<CreditAllocationReasonResponseDTO> reasons = service.getAllReasons(search, isActive, offset, limit);
        long total = service.getTotalReasonCount(search, isActive);

        AllResponseDto<List<CreditAllocationReasonResponseDTO>> allResponse =
                new AllResponseDto<>(offset, limit, total, reasons);

        return ResponseEntity.ok(
                new ApiResponseDto<>("Credit allocation reasons fetched successfully", 200, allResponse)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditAllocationReasonResponseDTO>> getReasonById(String id) {
        CreditAllocationReasonResponseDTO response = service.getReasonById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit allocation reason fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditAllocationReasonResponseDTO>> updateReason(
            String id,
            UpdateCreditAllocationReasonRequestDTO dto
    ) {
        CreditAllocationReasonResponseDTO response = service.updateReason(id, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit allocation reason updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteReason(String id) {
        service.deleteReason(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit allocation reason deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<CreditAllocationReasonDropdownDTO>>> getAllActiveReasons() {
        List<CreditAllocationReasonDropdownDTO> reasons = service.getAllActiveReasons();
        return ResponseEntity.ok(
                new ApiResponseDto<>("Active credit allocation reasons retrieved successfully", 200, reasons)
        );
    }
}

