package com.aspire.asat.billing.service;

import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.CommentLogResponseDTO;

import java.util.List;

public interface CommentLogService {

    CommentLogResponseDTO createCommentLog(CommentLogRequestDTO requestDTO);

    CommentLogResponseDTO getCommentLogById(String id);

    List<CommentLogResponseDTO> getCommentLogsByInvoiceId(String invoiceId);

    CommentLogResponseDTO updateCommentLog(String id, CommentLogRequestDTO requestDTO);

    void deleteCommentLog(String id);
}

