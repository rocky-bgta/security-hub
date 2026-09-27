package com.aspire.asat.billing.service;

import com.aspire.asat.common.dto.invoice_logs.NextStepRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.NextStepResponseDTO;

import java.util.List;

public interface NextStepService {

    NextStepResponseDTO createNextStep(NextStepRequestDTO requestDTO);

    NextStepResponseDTO getNextStepById(String id);

    List<NextStepResponseDTO> getAllNextSteps();

    NextStepResponseDTO updateNextStep(String id, NextStepRequestDTO requestDTO);

    void deleteNextStep(String id);
}

