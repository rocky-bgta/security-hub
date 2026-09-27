package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Invoice;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface InvoiceRepositoryCustom {
    List<Invoice> findInvoicesWithDynamicFilters(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId, Pageable pageable);

    List<Invoice> findInvoicesWithDynamicFilters(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId, int offset, int limit);

    long countInvoicesWithDynamicFilters(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId);

    Map<InvoiceStatus, Long> countInvoicesByStatus(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId);
}
