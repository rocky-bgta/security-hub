package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    // Find all invoices by clientAdminId with pagination
    Page<Invoice> findByClientAdminId(String clientAdminId, Pageable pageable);

    // Optional: for counting total invoices for a client (for pagination total)
    long countByClientAdminId(String clientAdminId);

    // Count invoices by status
    long countByStatus(InvoiceStatus status);

    // Count invoices by client admin and status
    long countByClientAdminIdAndStatus(String clientAdminId, InvoiceStatus status);

    // Optional: find invoice by paymentId
    Invoice findByPaymentId(String paymentId);

    // Optional: find all invoices by status
    List<Invoice> findByStatus(String status);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByClientAdminIdAndStatus(String clientAdminId, InvoiceStatus status, Pageable pageable);

    /** Unpaid invoices whose denormalized coupon expiry is before the given instant. */
    List<Invoice> findByStatusInAndCouponValidUntilBefore(
            Collection<InvoiceStatus> statuses, Instant cutoff);

    /**
     * Legacy unpaid reserved-coupon invoices missing denormalized {@code couponValidUntil}
     * (created before expiry was stored on the invoice).
     */
    List<Invoice> findByStatusInAndCouponUsageReservedTrueAndCouponValidUntilIsNull(
            Collection<InvoiceStatus> statuses);

    /** Unpaid invoices past their global {@code expiresAt}. */
    List<Invoice> findByStatusInAndExpiresAtBefore(
            Collection<InvoiceStatus> statuses, Instant cutoff);

    /** Unpaid invoices missing {@code expiresAt} (created before the field existed). */
    List<Invoice> findByStatusInAndExpiresAtIsNull(Collection<InvoiceStatus> statuses);

}
