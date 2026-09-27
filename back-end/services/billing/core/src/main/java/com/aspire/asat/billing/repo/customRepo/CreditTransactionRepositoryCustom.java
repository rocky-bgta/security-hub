package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.TransactionStatus;
import com.aspire.asat.billing.model.CreditTransaction;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CreditTransactionRepositoryCustom {
    List<CreditTransaction> findTransactionsWithDynamicFilters(String clientId,
                                                               String invoiceId,
                                                               TransactionStatus status,
                                                               String type,
                                                               Pageable pageable);

    long countTransactionsWithDynamicFilters(String clientId,
                                             String invoiceId,
                                             TransactionStatus status,
                                             String type);

    double calculateTotalAmountByType(String clientId, String type);

    double calculatePaidCreditByType(String clientId, String type);

    double calculateDueCreditByType(String clientId, String type);

    long countFilteredTransactions(String clientId, String invoiceId, TransactionStatus status, String type);

}
