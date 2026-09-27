package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.config.InvoiceExpiryProperties;
import com.aspire.asat.billing.constant.InvoiceReasonConstants;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.service.CouponService;
import com.aspire.asat.billing.service.InvoiceExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceExpiryServiceImpl implements InvoiceExpiryService {

    private static final List<InvoiceStatus> EXPIRABLE_STATUSES =
            List.of(InvoiceStatus.PENDING, InvoiceStatus.ON_PROGRESS);

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CouponService couponService;
    private final InvoiceExpiryProperties invoiceExpiryProperties;
    private final WebClient webClient;

    @Value("${service.registration.url}")
    private String registrationUrl;

    @Override
    public int expireOverdueInvoices() {
        Instant now = Instant.now();
        backfillMissingExpiresAt(now);

        List<Invoice> overdueInvoices = new ArrayList<>(
                invoiceRepository.findByStatusInAndExpiresAtBefore(EXPIRABLE_STATUSES, now));

        int expiredCount = 0;
        for (Invoice invoice : overdueInvoices) {
            try {
                List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
                if (hasSuccessfulPayment(payments)) {
                    continue;
                }

                if (invoice.isCouponUsageReserved() && invoice.getCouponId() != null) {
                    couponService.releaseCouponUsage(invoice.getCouponId());
                    clearCouponReservation(invoice);
                }

                invoice.setStatus(InvoiceStatus.EXPIRED);
                invoice.setReason(InvoiceReasonConstants.EXPIRED_UNPAID);
                invoiceRepository.save(invoice);
                cancelPendingPayments(invoice.getId(), payments);

                if (RoleType.CLIENT.equals(invoice.getRoleType())
                        && invoice.getClientAdminId() != null
                        && !invoice.getClientAdminId().isBlank()) {
                    expireClientLicense(invoice.getClientAdminId(), invoice.getClientProductIds());
                }

                expiredCount++;
                log.info("Marked invoice {} as EXPIRED (expiresAt: {})",
                        invoice.getId(), invoice.getExpiresAt());
            } catch (Exception e) {
                log.error("Failed to expire invoice {}: {}", invoice.getId(), e.getMessage(), e);
            }
        }

        return expiredCount;
    }

    /**
     * Older unpaid invoices may lack {@code expiresAt}. Set from createdAt + configured days
     * so they eventually expire instead of living forever.
     */
    private void backfillMissingExpiresAt(Instant now) {
        List<Invoice> missingExpiresAt = invoiceRepository
                .findByStatusInAndExpiresAtIsNull(EXPIRABLE_STATUSES);

        int days = invoiceExpiryProperties.getDays();
        for (Invoice invoice : missingExpiresAt) {
            Instant createdAt = invoice.getCreatedAt() != null ? invoice.getCreatedAt() : now;
            Instant expiresAt = createdAt.plus(days, ChronoUnit.DAYS);
            invoice.setExpiresAt(expiresAt);
            invoiceRepository.save(invoice);
        }
    }

    private void clearCouponReservation(Invoice invoice) {
        invoice.setCouponUsageReserved(false);
        invoice.setCouponId(null);
        invoice.setCouponReservedAt(null);
        invoice.setCouponValidUntil(null);
    }

    private void expireClientLicense(String clientId, List<String> clientProductIds) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("clientProductIds", clientProductIds != null ? clientProductIds : List.of());

            String url = registrationUrl + "/client/admin/expire-license/" + clientId;
            webClient.put()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Client license expiry cascaded via registration for client {} after unpaid invoice expiry",
                    clientId);
        } catch (WebClientResponseException e) {
            log.error("Failed to expire client license for {}: {} - {}",
                    clientId, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error expiring client license for {}: {}", clientId, e.getMessage(), e);
        }
    }

    private boolean hasSuccessfulPayment(List<Payment> payments) {
        return payments.stream()
                .anyMatch(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()));
    }

    private void cancelPendingPayments(String invoiceId, List<Payment> payments) {
        List<Payment> pendingPayments = payments.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus()))
                .toList();

        if (pendingPayments.isEmpty()) {
            return;
        }

        pendingPayments.forEach(p -> p.setStatus("CANCELLED"));
        paymentRepository.saveAll(pendingPayments);
        log.info("Cancelled {} pending payment(s) for expired invoice {}",
                pendingPayments.size(), invoiceId);
    }
}
