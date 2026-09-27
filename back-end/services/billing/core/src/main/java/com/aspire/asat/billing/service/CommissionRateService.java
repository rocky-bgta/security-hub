package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.CommissionRateRequestDTO;
import com.aspire.asat.billing.dto.CommissionRateResponseDTO;

import java.util.List;

public interface CommissionRateService {

    CommissionRateResponseDTO createCommissionRate(CommissionRateRequestDTO requestDTO);

    CommissionRateResponseDTO updateCommissionRate(String clientId, CommissionRateRequestDTO requestDTO);

    CommissionRateResponseDTO getCommissionRateByClientId(String clientId);

    List<CommissionRateResponseDTO> listCommissionRates(int offset, int limit);

    long countCommissionRates();

    void deleteCommissionRate(String clientId);

    void applyCommissionIfEligible(String clientId, Double invoiceAmount, String invoiceId);

}
