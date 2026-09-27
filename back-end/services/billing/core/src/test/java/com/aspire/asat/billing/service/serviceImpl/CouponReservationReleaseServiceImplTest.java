package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.constant.InvoiceReasonConstants;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponReservationReleaseServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CouponService couponService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CouponReservationReleaseServiceImpl releaseService;

    @BeforeEach
    void setUp() throws Exception {
        Field registrationUrlField = CouponReservationReleaseServiceImpl.class.getDeclaredField("registrationUrl");
        registrationUrlField.setAccessible(true);
        registrationUrlField.set(releaseService, "http://registration/api/v1");
    }

    @Test
    void cancelExpiredCouponInvoices_cancelsExpiredCouponInvoiceAndDeactivatesClient() {
        Instant expired = Instant.now().minus(1, ChronoUnit.DAYS);
        Invoice invoice = Invoice.builder()
                .id("INV-1")
                .clientAdminId("client-1")
                .clientProductIds(List.of("cp-1", "cp-2"))
                .couponId("coupon-1")
                .couponCode("SUMMER10")
                .couponUsageReserved(true)
                .couponValidUntil(expired)
                .status(InvoiceStatus.PENDING)
                .roleType(RoleType.CLIENT)
                .build();

        when(invoiceRepository.findByStatusInAndCouponValidUntilBefore(any(), any()))
                .thenReturn(List.of(invoice));
        when(invoiceRepository.findByStatusInAndCouponUsageReservedTrueAndCouponValidUntilIsNull(any()))
                .thenReturn(List.of());
        when(paymentRepository.findByInvoiceId("INV-1")).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        stubDeactivateWebClient();

        int cancelled = releaseService.cancelExpiredCouponInvoices();

        assertEquals(1, cancelled);
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertEquals(InvoiceStatus.CANCELLED, captor.getValue().getStatus());
        assertEquals(InvoiceReasonConstants.CANCELLED_COUPON_EXPIRED, captor.getValue().getReason());
        assertEquals(false, captor.getValue().isCouponUsageReserved());
        verify(couponService).releaseCouponUsage("coupon-1");
        verify(webClient).put();
    }

    @Test
    void cancelExpiredCouponInvoices_skipsWhenSuccessfulPaymentExists() {
        Invoice invoice = Invoice.builder()
                .id("INV-2")
                .clientAdminId("client-1")
                .couponId("coupon-1")
                .couponUsageReserved(true)
                .couponValidUntil(Instant.now().minus(1, ChronoUnit.HOURS))
                .status(InvoiceStatus.PENDING)
                .roleType(RoleType.CLIENT)
                .build();
        Payment success = new Payment();
        success.setStatus("SUCCESS");

        when(invoiceRepository.findByStatusInAndCouponValidUntilBefore(any(), any()))
                .thenReturn(List.of(invoice));
        when(invoiceRepository.findByStatusInAndCouponUsageReservedTrueAndCouponValidUntilIsNull(any()))
                .thenReturn(List.of());
        when(paymentRepository.findByInvoiceId("INV-2")).thenReturn(List.of(success));

        int cancelled = releaseService.cancelExpiredCouponInvoices();

        assertEquals(0, cancelled);
        verify(couponService, never()).releaseCouponUsage(anyString());
        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(webClient, never()).put();
    }

    @Test
    void cancelExpiredCouponInvoices_skipsWhenCouponStillValid() {
        when(invoiceRepository.findByStatusInAndCouponValidUntilBefore(any(), any()))
                .thenReturn(List.of());
        when(invoiceRepository.findByStatusInAndCouponUsageReservedTrueAndCouponValidUntilIsNull(any()))
                .thenReturn(List.of());

        int cancelled = releaseService.cancelExpiredCouponInvoices();

        assertEquals(0, cancelled);
        verify(couponService, never()).releaseCouponUsage(anyString());
        verify(webClient, never()).put();
    }

    @Test
    void cancelExpiredCouponInvoices_cancelsMspInvoiceWithoutDeactivateCall() {
        Invoice invoice = Invoice.builder()
                .id("INV-MSP")
                .mspAdminId("msp-1")
                .couponId("coupon-1")
                .couponUsageReserved(true)
                .couponValidUntil(Instant.now().minus(1, ChronoUnit.HOURS))
                .status(InvoiceStatus.ON_PROGRESS)
                .roleType(RoleType.MSP)
                .build();

        when(invoiceRepository.findByStatusInAndCouponValidUntilBefore(any(), any()))
                .thenReturn(List.of(invoice));
        when(invoiceRepository.findByStatusInAndCouponUsageReservedTrueAndCouponValidUntilIsNull(any()))
                .thenReturn(List.of());
        when(paymentRepository.findByInvoiceId("INV-MSP")).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        int cancelled = releaseService.cancelExpiredCouponInvoices();

        assertEquals(1, cancelled);
        verify(couponService).releaseCouponUsage("coupon-1");
        verify(webClient, never()).put();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubDeactivateWebClient() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        org.mockito.Mockito.doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());
    }
}
