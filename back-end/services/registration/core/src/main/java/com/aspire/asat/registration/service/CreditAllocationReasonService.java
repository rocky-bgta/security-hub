package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.creditAllocation.CreateCreditAllocationReasonRequestDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonDropdownDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonResponseDTO;
import com.aspire.asat.registration.data.creditAllocation.UpdateCreditAllocationReasonRequestDTO;

import java.util.List;

public interface CreditAllocationReasonService {

    CreditAllocationReasonResponseDTO createReason(CreateCreditAllocationReasonRequestDTO dto);

    List<CreditAllocationReasonResponseDTO> getAllReasons(String search, Boolean isActive, int offset, int limit);

    long getTotalReasonCount(String search, Boolean isActive);

    CreditAllocationReasonResponseDTO getReasonById(String id);

    CreditAllocationReasonResponseDTO updateReason(String id, UpdateCreditAllocationReasonRequestDTO dto);

    void deleteReason(String id);

    List<CreditAllocationReasonDropdownDTO> getAllActiveReasons();
}

