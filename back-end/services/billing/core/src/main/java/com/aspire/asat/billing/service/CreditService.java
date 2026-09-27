package com.aspire.asat.billing.service;


import com.aspire.asat.billing.dto.*;

import java.util.List;

public interface CreditService {

    CreditResponseDTO createCredit(CreditCreateRequestDTO requestDTO);

    CreditResponseDTO updateCredit(String creditId, CreditUpdateRequestDTO requestDTO);

    CreditResponseDTO getCreditById(String creditId);

    List<CreditResponseDTO> getCreditsByClientId(String clientId);

    CreditResponseDTO deactivateCredit(String creditId);

    void deleteCredit(String creditId);

    CreditTransactionResponseDTO logCreditTransaction(CreditTransactionCreateDTO transactionDTO);

    CreditTransactionResponseDTO getCreditTransactionById(String transactionId);

    List<CreditTransactionResponseDTO> getTransactionsByCreditId(String creditId);

    List<CreditTransactionResponseDTO> getTransactionsByClientId(String clientId);

    CreditTransferResponseDTO transferCredit(CreditTransferRequestDTO requestDTO);

    CreditResponseDTO depositCredit(CreditOperationRequestDTO requestDTO);

    CreditResponseDTO withdrawCredit(CreditOperationRequestDTO requestDTO);

    List<CreditTransactionResponseDTO> getCreditUsageSummaryByClientId(
            String clientId,
            String invoiceId,
            TransactionStatus status,
            String type,
            int offset,
            int limit
    );

    long countCreditUsageSummaryByClientId(
            String clientId,
            String invoiceId,
            TransactionStatus status,
            String type
    );

    CreditUsageSummaryDTO getCreditUsageTotalsByClientId(String clientId, String type);

    CreditTransactionResponseDTO updateCreditTransaction(CreditTransactionUpdateRequestDTO dto);



}
