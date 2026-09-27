package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    @Query("{ 'paymentSources.transactionId': ?0 }")
    Optional<Payment> findByPaymentSourceTransactionId(String transactionId);

    @Query("{ 'paymentSources.method': 'CREDIT' }")
    List<Payment> findAllCreditPayments();

    List<Payment> findByStatus(String status); // already possible by default Spring Data

    @Query("{ $and: ["
            + "{ $or: [ { 'status': ?0 }, { ?0: null } ] },"
            + "{ $or: [ { 'paymentSources.method': ?1 }, { ?1: null } ] },"
            + "{ $or: [ { 'clientId': ?4 }, { ?4: null } ] },"
            + "{ $or: [ { 'createdAt': { $gte: ?2 } }, { ?2: null } ] },"
            + "{ $or: [ { 'createdAt': { $lte: ?3 } }, { ?3: null } ] }"
            + "] }")
    List<Payment> findPaymentsWithFilters(String status, String method, Instant startDate, Instant endDate, String clientId, Pageable pageable);

    @Query("{ $and: ["
            + "{ $or: [ { 'status': ?0 }, { ?0: null } ] },"
            + "{ $or: [ { 'paymentSources.method': ?1 }, { ?1: null } ] },"
            + "{ $or: [ { 'clientId': ?4 }, { ?4: null } ] },"
            + "{ $or: [ { 'createdAt': { $gte: ?2 } }, { ?2: null } ] },"
            + "{ $or: [ { 'createdAt': { $lte: ?3 } }, { ?3: null } ] }"
            + "] }")
    long countPaymentsWithFilters(String status, String method, Instant startDate, Instant endDate, String clientId);

    List<Payment> findByInvoiceId(String invoiceId);

    @Query("{ 'invoiceId': { $in: ?0 }, 'status': 'SUCCESS', 'amount': { $ne: null } }")
    List<Payment> findByInvoiceIdInAndStatusSuccess(List<String> invoiceIds);

    @Query("{ 'status': 'PENDING', 'online': true, 'transactionId': { $exists: true, $ne: null } }")
    List<Payment> findPendingOnlinePaymentsWithTransactionId();

    @Query("{ 'status': 'PENDING', 'online': true, 'transactionId': { $exists: true, $ne: null }, 'createdAt': { $gte: ?0 } }")
    List<Payment> findPendingOnlinePaymentsWithTransactionIdSince(Instant since);

}
