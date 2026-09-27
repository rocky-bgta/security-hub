package com.aspire.asat.billing.service;

import com.aspire.asat.common.dto.invoice_logs.ActionRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.ActionResponseDTO;

import java.util.List;

public interface ActionService {

    ActionResponseDTO createAction(ActionRequestDTO requestDTO);

    ActionResponseDTO getActionById(String id);

    List<ActionResponseDTO> getAllActions();

    ActionResponseDTO updateAction(String id, ActionRequestDTO requestDTO);

    void deleteAction(String id);
}

