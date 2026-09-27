package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.constant.InvoiceReasonConstants;
import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.service.CouponReservationReleaseService;
import com.aspire.asat.billing.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponReservationReleaseServiceImpl implements CouponReservationReleaseService {

    private static final List<InvoiceStatus> CANCELLABLE_STATUSES =
            List.of(InvoiceStatus.PENDING, InvoiceStatus.ON_PROGRESS);

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CouponService couponService;
    private final WebClient webClient;

    @Value("${service.registration.url}")
    private String registrationUrl;

    @Override
    public int cancelExpiredCouponInvoices() {
        Instant now = Instant.now();

        List<Invoice> expiredInvoices = new ArrayList<>(
                invoiceRepository.findByStatusInAndCouponValidUntilBefore(CANCELLABLE_STATUSES, now));

        backfillAndCollectLegacyExpiredInvoices(expiredInvoices, now);

        int cancelledCount = 0;
        for (Invoice invoice : expiredInvoices) {
            try {
                if (hasSuccessfulPayment(invoice.getId())) {
                    continue;
                }

                if (invoice.isCouponUsageReserved() && invoice.getCouponId() != null) {
                    couponService.releaseCouponUsage(invoice.getCouponId());
                }
                clearCouponReservation(invoice);
                invoice.setStatus(InvoiceStatus.CANCELLED);
                invoice.setReason(InvoiceReasonConstants.CANCELLED_COUPON_EXPIRED);
                invoiceRepository.save(invoice);

                if (RoleType.CLIENT.equals(invoice.getRoleType())
                        && invoice.getClientAdminId() != null
                        && !invoice.getClientAdminId().isBlank()) {
                    deactivateClientAdmin(invoice.getClientAdminId(), invoice.getClientProductIds());
                }

                cancelledCount++;
                log.info("Cancelled invoice {} after coupon expiry (couponCode: {})",
                        invoice.getId(), invoice.getCouponCode());
            } catch (Exception e) {
                log.error("Failed to cancel expired-coupon invoice {}: {}",
                        invoice.getId(), e.getMessage(), e);
            }
        }

        return cancelledCount;
    }

    /**
     * Older invoices may have a reserved coupon without denormalized {@code couponValidUntil}.
     * Resolve from the coupon document once, persist for future runs, and cancel if already expired.
     */
    private void backfillAndCollectLegacyExpiredInvoices(List<Invoice> expiredInvoices, Instant now) {
        List<Invoice> legacyInvoices = invoiceRepository
                .findByStatusInAndCouponUsageReservedTrueAndCouponValidUntilIsNull(CANCELLABLE_STATUSES);

        for (Invoice invoice : legacyInvoices) {
            if (invoice.getCouponId() == null || invoice.getCouponId().isBlank()) {
                continue;
            }
            Instant validUntil = resolveCouponValidUntil(invoice.getCouponId());
            if (validUntil == null) {
                continue;
            }
            invoice.setCouponValidUntil(validUntil);
            if (validUntil.isBefore(now)) {
                expiredInvoices.add(invoice);
            } else {
                invoiceRepository.save(invoice);
            }
        }
    }

    private void clearCouponReservation(Invoice invoice) {
        invoice.setCouponUsageReserved(false);
        invoice.setCouponId(null);
        invoice.setCouponReservedAt(null);
        invoice.setCouponValidUntil(null);
    }

    private Instant resolveCouponValidUntil(String couponId) {
        try {
            CouponCreateResponseDTO coupon = couponService.getCouponById(couponId);
            return coupon != null ? coupon.getValidUntil() : null;
        } catch (Exception e) {
            log.warn("Could not resolve couponValidUntil for couponId {}: {}", couponId, e.getMessage());
            return null;
        }
    }

    private void deactivateClientAdmin(String clientId, List<String> clientProductIds) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("clientProductIds", clientProductIds != null ? clientProductIds : List.of());

            String url = registrationUrl + "/client/admin/deactivate-license/" + clientId;
            webClient.put()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Client admin {} deactivated via registration after coupon-expiry invoice cancel", clientId);
        } catch (WebClientResponseException e) {
            log.error("Failed to deactivate client admin {}: {} - {}",
                    clientId, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error deactivating client admin {}: {}", clientId, e.getMessage(), e);
        }
    }

    private boolean hasSuccessfulPayment(String invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .anyMatch(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()));
    }
}
