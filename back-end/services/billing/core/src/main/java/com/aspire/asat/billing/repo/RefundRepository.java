package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.dto.refund.RefundStatus;
import com.aspire.asat.billing.model.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RefundRepository extends MongoRepository<Refund, String> {

    // Find refunds by payment ID
    List<Refund> findByPaymentId(String paymentId);

    // Find refunds by invoice ID
    List<Refund> findByInvoiceId(String invoiceId);

    // Find refunds by client ID
    List<Refund> findByClientId(String clientId);

    // Find refunds by status
    List<Refund> findByStatus(RefundStatus status);

    // Find refunds by client ID with pagination
    Page<Refund> findByClientId(String clientId, Pageable pageable);

    // Find refunds within date range
    List<Refund> findByCreatedAtBetween(Instant startDate, Instant endDate);

    // Find refunds by status within date range
    List<Refund> findByStatusAndCreatedAtBetween(RefundStatus status, Instant startDate, Instant endDate);

    // Count refunds by status
    long countByStatus(RefundStatus status);

    // Count refunds within date range
    long countByCreatedAtBetween(Instant startDate, Instant endDate);

    // Sum refund amounts within date range (will be done in service layer with aggregation)
}

