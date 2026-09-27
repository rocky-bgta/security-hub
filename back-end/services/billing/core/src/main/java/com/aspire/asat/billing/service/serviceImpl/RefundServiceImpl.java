package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.refund.RefundRequestDTO;
import com.aspire.asat.billing.dto.refund.RefundResponseDTO;
import com.aspire.asat.billing.dto.refund.RefundStatus;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.model.Refund;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.repo.RefundRepository;
import com.aspire.asat.billing.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public RefundResponseDTO createRefund(RefundRequestDTO requestDTO, String processedBy) {
        log.info("Creating refund for payment: {}, invoice: {}", requestDTO.getPaymentId(), requestDTO.getInvoiceId());

        // Validate payment exists
        Payment payment = paymentRepository.findById(requestDTO.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + requestDTO.getPaymentId()));

        // Validate invoice exists
        if (!invoiceRepository.existsById(requestDTO.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found: " + requestDTO.getInvoiceId());
        }

        // Validate refund amount doesn't exceed payment amount
        if (requestDTO.getRefundAmount() > payment.getAmount()) {
            throw new BillingServiceException(
                    "Refund amount cannot exceed payment amount",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check if refund already exists for this payment
        List<Refund> existingRefunds = refundRepository.findByPaymentId(requestDTO.getPaymentId());
        double totalRefunded = existingRefunds.stream()
                .filter(r -> r.getStatus() != RefundStatus.FAILED)
                .mapToDouble(Refund::getRefundAmount)
                .sum();

        if (totalRefunded + requestDTO.getRefundAmount() > payment.getAmount()) {
            throw new BillingServiceException(
                    String.format("Total refund amount (%.2f) would exceed payment amount (%.2f)",
                            totalRefunded + requestDTO.getRefundAmount(), payment.getAmount()),
                    HttpStatus.BAD_REQUEST
            );
        }

        Refund refund = Refund.builder()
                .paymentId(requestDTO.getPaymentId())
                .invoiceId(requestDTO.getInvoiceId())
                .clientId(payment.getClientId())
                .mspAdminId(payment.getMspAdminId())
                .countryId(payment.getCountryId())
                .refundAmount(requestDTO.getRefundAmount())
                .currency(requestDTO.getCurrency() != null ? requestDTO.getCurrency() : payment.getCurrency())
                .reason(requestDTO.getReason())
                .status(RefundStatus.PENDING)
                .createdAt(Instant.now())
                .processedBy(processedBy)
                .notes(requestDTO.getNotes())
                .build();

        Refund savedRefund = refundRepository.save(refund);
        log.info("Refund created with ID: {}", savedRefund.getId());

        return mapToResponseDTO(savedRefund);
    }

    @Override
    public RefundResponseDTO getRefundById(String id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + id));
        return mapToResponseDTO(refund);
    }

    @Override
    public List<RefundResponseDTO> getRefunds(String clientId, String mspId, String countryId,
                                               RefundStatus status, String startDate, String endDate,
                                               int offset, int limit) {
        // Validate limit is positive
        if (limit <= 0) {
            throw new BillingServiceException(
                "Limit must be greater than 0", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate offset is non-negative
        if (offset < 0) {
            throw new BillingServiceException(
                "Offset must be greater than or equal to 0", 
                HttpStatus.BAD_REQUEST
            );
        }

        // Convert offset (page number) to actual skip count for pagination
        // offset=0 → page 0 → skip 0, take limit
        // offset=1 → page 1 → skip limit, take limit
        // offset=2 → page 2 → skip 2*limit, take limit
        int skip = offset * limit;

        Query query = buildQuery(clientId, mspId, countryId, status, startDate, endDate);
        query.skip(skip).limit(limit);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Refund> refunds = mongoTemplate.find(query, Refund.class);
        return refunds.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countRefunds(String clientId, String mspId, String countryId,
                              RefundStatus status, String startDate, String endDate) {
        Query query = buildQuery(clientId, mspId, countryId, status, startDate, endDate);
        return mongoTemplate.count(query, Refund.class);
    }

    private Query buildQuery(String clientId, String mspId, String countryId,
                              RefundStatus status, String startDate, String endDate) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (clientId != null && !clientId.isBlank()) {
            criteriaList.add(Criteria.where("clientId").is(clientId));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        if (startDate != null && !startDate.isBlank()) {
            Instant start = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            criteriaList.add(Criteria.where("createdAt").gte(start));
        }
        if (endDate != null && !endDate.isBlank()) {
            Instant end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            criteriaList.add(Criteria.where("createdAt").lt(end));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    @Override
    public List<RefundResponseDTO> getRefundsByPaymentId(String paymentId) {
        return refundRepository.findByPaymentId(paymentId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundResponseDTO> getRefundsByInvoiceId(String invoiceId) {
        return refundRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RefundResponseDTO processRefund(String refundId, RefundStatus status, String transactionId) {
        log.info("Processing refund: {} with status: {}", refundId, status);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new BillingServiceException(
                    "Refund is not in PENDING status, cannot process",
                    HttpStatus.BAD_REQUEST
            );
        }

        refund.setStatus(status);
        if (status == RefundStatus.PROCESSED) {
            refund.setRefundedAt(Instant.now());
        }
        if (transactionId != null) {
            refund.setTransactionId(transactionId);
        }

        Refund updatedRefund = refundRepository.save(refund);
        log.info("Refund {} processed with status: {}", refundId, status);

        return mapToResponseDTO(updatedRefund);
    }

    private RefundResponseDTO mapToResponseDTO(Refund refund) {
        return RefundResponseDTO.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .invoiceId(refund.getInvoiceId())
                .clientId(refund.getClientId())
                .mspAdminId(refund.getMspAdminId())
                .countryId(refund.getCountryId())
                .refundAmount(refund.getRefundAmount())
                .currency(refund.getCurrency())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .refundedAt(refund.getRefundedAt())
                .createdAt(refund.getCreatedAt())
                .processedBy(refund.getProcessedBy())
                .transactionId(refund.getTransactionId())
                .notes(refund.getNotes())
                .build();
    }
}

