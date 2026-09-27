package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Payment;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface PaymentRepositoryCustom {
    List<Payment> findPaymentsWithDynamicFilters(List<String> statuses, String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search, Pageable pageable);

    List<Payment> findPaymentsWithDynamicFilters(List<String> statuses, String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search, int offset, int limit);

    long countPaymentsWithDynamicFilters(List<String> statuses, String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search);

    PaymentSummaryResult getPaymentSummary(Instant startDate, Instant endDate, String clientId, String mspId,
            String countryId, RoleType roleType);

    Map<String, PaymentStatusAggregate> aggregatePaymentsByStatus(String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search, List<String> statuses);
}
