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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceExpiryServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CouponService couponService;
    @Mock
    private InvoiceExpiryProperties invoiceExpiryProperties;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private InvoiceExpiryServiceImpl expiryService;

    @BeforeEach
    void setUp() throws Exception {
        Field registrationUrlField = InvoiceExpiryServiceImpl.class.getDeclaredField("registrationUrl");
        registrationUrlField.setAccessible(true);
        registrationUrlField.set(expiryService, "http://registration/api/v1");
        org.mockito.Mockito.lenient().when(invoiceExpiryProperties.getDays()).thenReturn(30);
    }

    @Test
    void expireOverdueInvoices_marksExpiredAndCallsExpireLicenseForClient() {
        Instant expiredAt = Instant.now().minus(1, ChronoUnit.DAYS);
        Invoice invoice = Invoice.builder()
                .id("INV-1")
                .clientAdminId("client-1")
                .clientProductIds(List.of("cp-1"))
                .couponId("coupon-1")
                .couponUsageReserved(true)
                .expiresAt(expiredAt)
                .status(InvoiceStatus.PENDING)
                .roleType(RoleType.CLIENT)
                .build();
        Payment pendingPayment = new Payment();
        pendingPayment.setId("PAY-1");
        pendingPayment.setStatus("PENDING");

        when(invoiceRepository.findByStatusInAndExpiresAtIsNull(any())).thenReturn(List.of());
        when(invoiceRepository.findByStatusInAndExpiresAtBefore(any(), any()))
                .thenReturn(List.of(invoice));
        when(paymentRepository.findByInvoiceId("INV-1")).thenReturn(List.of(pendingPayment));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        stubExpireWebClient();

        int expired = expiryService.expireOverdueInvoices();

        assertEquals(1, expired);
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertEquals(InvoiceStatus.EXPIRED, captor.getValue().getStatus());
        assertEquals(InvoiceReasonConstants.EXPIRED_UNPAID, captor.getValue().getReason());
        assertEquals(false, captor.getValue().isCouponUsageReserved());
        verify(couponService).releaseCouponUsage("coupon-1");
        verify(webClient).put();

        ArgumentCaptor<List<Payment>> paymentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paymentRepository).saveAll(paymentCaptor.capture());
        assertEquals("CANCELLED", paymentCaptor.getValue().get(0).getStatus());
    }

    @Test
    void expireOverdueInvoices_skipsWhenSuccessfulPaymentExists() {
        Invoice invoice = Invoice.builder()
                .id("INV-2")
                .clientAdminId("client-1")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .status(InvoiceStatus.PENDING)
                .roleType(RoleType.CLIENT)
                .build();
        Payment success = new Payment();
        success.setStatus("SUCCESS");

        when(invoiceRepository.findByStatusInAndExpiresAtIsNull(any())).thenReturn(List.of());
        when(invoiceRepository.findByStatusInAndExpiresAtBefore(any(), any()))
                .thenReturn(List.of(invoice));
        when(paymentRepository.findByInvoiceId("INV-2")).thenReturn(List.of(success));

        int expired = expiryService.expireOverdueInvoices();

        assertEquals(0, expired);
        verify(couponService, never()).releaseCouponUsage(anyString());
        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(paymentRepository, never()).saveAll(anyList());
        verify(webClient, never()).put();
    }

    @Test
    void expireOverdueInvoices_expiresMspInvoiceWithoutRegistrationCall() {
        Invoice invoice = Invoice.builder()
                .id("INV-MSP")
                .mspAdminId("msp-1")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .status(InvoiceStatus.ON_PROGRESS)
                .roleType(RoleType.MSP)
                .build();
        Payment pendingPayment = new Payment();
        pendingPayment.setId("PAY-MSP");
        pendingPayment.setStatus("PENDING");

        when(invoiceRepository.findByStatusInAndExpiresAtIsNull(any())).thenReturn(List.of());
        when(invoiceRepository.findByStatusInAndExpiresAtBefore(any(), any()))
                .thenReturn(List.of(invoice));
        when(paymentRepository.findByInvoiceId("INV-MSP")).thenReturn(List.of(pendingPayment));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        int expired = expiryService.expireOverdueInvoices();

        assertEquals(1, expired);
        verify(webClient, never()).put();

        ArgumentCaptor<List<Payment>> paymentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paymentRepository).saveAll(paymentCaptor.capture());
        assertEquals("CANCELLED", paymentCaptor.getValue().get(0).getStatus());
    }

    @Test
    void expireOverdueInvoices_backfillsNullExpiresAt() {
        Instant createdAt = Instant.now().minus(40, ChronoUnit.DAYS);
        Invoice legacy = Invoice.builder()
                .id("INV-LEGACY")
                .clientAdminId("client-1")
                .createdAt(createdAt)
                .status(InvoiceStatus.PENDING)
                .roleType(RoleType.CLIENT)
                .build();

        when(invoiceRepository.findByStatusInAndExpiresAtIsNull(any())).thenReturn(List.of(legacy));
        when(invoiceRepository.findByStatusInAndExpiresAtBefore(any(), any())).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        expiryService.expireOverdueInvoices();

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertEquals(createdAt.plus(30, ChronoUnit.DAYS), captor.getValue().getExpiresAt());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubExpireWebClient() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        org.mockito.Mockito.doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());
    }
}
