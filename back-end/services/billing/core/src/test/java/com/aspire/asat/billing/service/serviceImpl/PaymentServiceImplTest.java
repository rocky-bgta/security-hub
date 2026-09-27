package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.ClientInfoRequestDTO;
import com.aspire.asat.billing.dto.PaymentHistoryItemDTO;
import com.aspire.asat.billing.dto.PaymentRequestDTO;
import com.aspire.asat.billing.dto.PaymentSummaryReportDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.model.ClientInfo;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.ClientInfoRepository;
import com.aspire.asat.billing.repo.CreditRepository;
import com.aspire.asat.billing.repo.CreditTransactionRepository;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.repo.customRepo.PaymentRepositoryCustom;
import com.aspire.asat.billing.repo.customRepo.PaymentStatusAggregate;
import com.aspire.asat.billing.service.CommissionRateService;
import com.aspire.asat.billing.service.CouponService;
import com.aspire.asat.billing.service.CreditService;
import com.aspire.asat.billing.service.InvoiceService;
import com.aspire.asat.billing.utils.PaymentNotificationUtil;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.billing.utils.file.AzureBlobUploader;
import com.aspire.asat.billing.utils.file.PdfGeneratorService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private static final String REGISTRATION_BASE_URL = "http://registration/api/v1";

    @Mock private ClientInfoRepository clientInfoRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PdfGeneratorService pdfGeneratorService;
    @Mock private AzureBlobUploader azureBlobUploader;
    @Mock private CouponService couponService;
    @Mock private CreditRepository creditRepository;
    @Mock private CreditService creditService;
    @Mock private CommissionRateService commissionRateService;
    @Mock private CreditTransactionRepository creditTransactionRepository;
    @Mock private InvoiceService invoiceService;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private WebClient webClient;
    @Mock private PaymentRepositoryCustom paymentRepositoryCustom;
    @Mock private ObjectMapper objectMapper;
    @Mock private PaymentNotificationUtil paymentNotificationUtil;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        setField(paymentService, "registrationUrl", REGISTRATION_BASE_URL);
    }

    @Test
    void toPaymentHistoryItem_withCouponInvoice_usesInvoiceVat() {
        Invoice invoice = Invoice.builder()
                .id("INV-1781275820401")
                .clientName("Aspire")
                .mspName("Aspire")
                .countryName("USA")
                .subtotal(750.0)
                .discountAmount(0.0)
                .couponDiscountAmount(75.0)
                .vatAmount(81.0)
                .totalAmount(756.0)
                .createdAt(Instant.parse("2026-06-12T14:50:20.402Z"))
                .build();

        Payment payment = new Payment();
        payment.setId("pay-1");
        payment.setInvoiceId("INV-1781275820401");
        payment.setAmount(756.0);
        payment.setStatus("SUCCESS");
        payment.setSubtotal(750.0);
        payment.setVatAmount(90.0);
        payment.setDiscountAmount(0.0);
        payment.setRoleType(RoleType.CLIENT);
        payment.setCreatedAt(Instant.parse("2026-06-12T14:57:35.583Z"));

        PaymentHistoryItemDTO result = paymentService.toPaymentHistoryItem(payment, invoice);

        assertEquals(750.0, result.getSubtotal());
        assertEquals(0.0, result.getDiscountAmount());
        assertEquals(75.0, result.getCouponDiscountAmount());
        assertEquals(81.0, result.getVatAmount());
        assertEquals(756.0, result.getTotalAmount());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    void toPaymentHistoryItem_withoutInvoice_fallsBackToPayment() {
        Payment payment = new Payment();
        payment.setId("pay-2");
        payment.setInvoiceId("INV-MISSING");
        payment.setSubtotal(500.0);
        payment.setVatAmount(50.0);
        payment.setDiscountAmount(25.0);
        payment.setAmount(525.0);
        payment.setStatus("PENDING");

        PaymentHistoryItemDTO result = paymentService.toPaymentHistoryItem(payment, null);

        assertEquals(500.0, result.getSubtotal());
        assertEquals(50.0, result.getVatAmount());
        assertEquals(25.0, result.getDiscountAmount());
        assertNull(result.getCouponDiscountAmount());
    }

    @Test
    void toPaymentHistoryItem_computesOutstandingFromInvoiceTotal() {
        Invoice invoice = Invoice.builder()
                .totalAmount(1000.0)
                .subtotal(1000.0)
                .build();

        Payment payment = new Payment();
        payment.setAmount(400.0);

        PaymentHistoryItemDTO result = paymentService.toPaymentHistoryItem(payment, invoice);

        assertEquals(1000.0, result.getTotalAmount());
        assertEquals(600.0, result.getOutstanding());
    }

    @Test
    void populatePaymentFinancialsFromInvoice_copiesAllFields() {
        Invoice invoice = Invoice.builder()
                .subtotal(750.0)
                .discountAmount(0.0)
                .couponDiscountAmount(75.0)
                .vatAmount(81.0)
                .couponCode("ASAT50")
                .couponId("coupon-1")
                .build();

        Payment payment = new Payment();

        paymentService.populatePaymentFinancialsFromInvoice(payment, invoice);

        assertEquals(750.0, payment.getSubtotal());
        assertEquals(81.0, payment.getVatAmount());
        assertEquals(75.0, payment.getDiscountAmount());
        assertEquals("ASAT50", payment.getCouponCode());
        assertEquals("coupon-1", payment.getCouponId());
        assertNull(payment.getBreakdown());
    }

    @Test
    void populatePaymentFinancialsFromInvoice_sumsInvoiceAndCouponDiscount() {
        Invoice invoice = Invoice.builder()
                .subtotal(1000.0)
                .discountAmount(50.0)
                .couponDiscountAmount(100.0)
                .vatAmount(85.0)
                .build();

        Payment payment = new Payment();

        paymentService.populatePaymentFinancialsFromInvoice(payment, invoice);

        assertEquals(150.0, payment.getDiscountAmount());
    }

    @Test
    void saveClientInfo_persistsClientInfo() {
        ClientInfoRequestDTO request = new ClientInfoRequestDTO();
        request.setClientName("Acme Corp");
        request.setContactEmail("billing@acme.com");
        request.setPhone("+1234567890");
        request.setBusinessName("Acme");
        request.setAddress("123 Main St");

        when(clientInfoRepository.save(any(ClientInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientInfo saved = paymentService.saveClientInfo(request);

        assertEquals("Acme Corp", saved.getClientName());
        assertEquals("billing@acme.com", saved.getContactEmail());
        verify(clientInfoRepository).save(any(ClientInfo.class));
    }

    @Test
    void calculateBill_throwsUnsupportedOperationException() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        assertThrows(UnsupportedOperationException.class, () -> paymentService.calculateBill(request));
    }

    @Test
    void getPaymentHistory_throwsWhenLimitNotPositive() {
        stubSuperAdminContext();

        BillingServiceException ex = assertThrows(
                BillingServiceException.class,
                () -> paymentService.getPaymentHistory(null, null, null, null, null, null, null, null, null, 0, 0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void getPaymentHistory_throwsWhenOffsetNegative() {
        stubSuperAdminContext();

        BillingServiceException ex = assertThrows(
                BillingServiceException.class,
                () -> paymentService.getPaymentHistory(null, null, null, null, null, null, null, null, null, -1, 10)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void getPaymentSummaryReport_mapsStatusCountsAndAmounts_missingStatusesDefaultToZero() {
        stubSuperAdminContext();

        Map<String, PaymentStatusAggregate> aggregates = new HashMap<>();
        aggregates.put("SUCCESS", PaymentStatusAggregate.builder().count(2L).amountSum(150.0).build());
        aggregates.put("PENDING", PaymentStatusAggregate.builder().count(1L).amountSum(50.0).build());
        aggregates.put("FAILED", PaymentStatusAggregate.builder().count(1L).amountSum(25.0).build());

        when(paymentRepositoryCustom.aggregatePaymentsByStatus(
                isNull(), any(Instant.class), any(Instant.class), eq("client-1"), isNull(),
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(aggregates);

        PaymentSummaryReportDTO summary = paymentService.getPaymentSummaryReport(
                null, "client-1", null, null, null, "2026-01-01", "2026-01-31", null);

        assertEquals(4L, summary.getTotalPaymentCount());
        assertEquals(2L, summary.getSuccessPaymentCount());
        assertEquals(1L, summary.getPendingPaymentCount());
        assertEquals(1L, summary.getFailedPaymentCount());
        assertEquals(0L, summary.getCancelledPaymentCount());
        assertEquals(150.0, summary.getTotalSuccessAmount());
        assertEquals(50.0, summary.getTotalPendingAmount());
        assertEquals(25.0, summary.getTotalFailedAmount());
        assertEquals(0.0, summary.getTotalCancelledAmount());
    }

    @Test
    void getPaymentSummaryReport_mspUserForcesMspId() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userType(UserType.MSP.name()).userId("msp-forced").build()
        );

        when(paymentRepositoryCustom.aggregatePaymentsByStatus(
                isNull(), nullable(Instant.class), nullable(Instant.class), isNull(), eq("msp-forced"),
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(Collections.emptyMap());

        paymentService.getPaymentSummaryReport(
                null, null, "msp-from-request", null, null, null, null, null);

        verify(paymentRepositoryCustom).aggregatePaymentsByStatus(
                isNull(), nullable(Instant.class), nullable(Instant.class), isNull(), eq("msp-forced"),
                isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void getPaymentSummaryReportList_delegatesWithResolvedQuickRangeAndClientAdminId() {
        stubSuperAdminContext();

        when(paymentRepositoryCustom.findPaymentsWithDynamicFilters(
                isNull(), isNull(), any(Instant.class), any(Instant.class),
                eq("client-admin-1"), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()
        )).thenReturn(Collections.emptyList());

        paymentService.getPaymentSummaryReportList(
                null, "client-admin-1", null, null, null,
                "2020-01-01", "2020-01-02", QuickRange.LAST_7_DAYS, 0, 20);

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(paymentRepositoryCustom).findPaymentsWithDynamicFilters(
                isNull(), isNull(), startCaptor.capture(), endCaptor.capture(),
                eq("client-admin-1"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20));

        LocalDate today = LocalDate.now();
        LocalDate expectedStart = today.minusDays(7);
        assertEquals(expectedStart.toString(),
                startCaptor.getValue().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString());
        assertEquals(today.toString(),
                endCaptor.getValue().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString());
    }

    @Test
    void countPaymentSummaryReport_mspUserForcesMspId() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userType(UserType.MSP.name()).userId("msp-forced").build()
        );

        when(paymentRepositoryCustom.countPaymentsWithDynamicFilters(
                eq(List.of("SUCCESS")), isNull(), nullable(Instant.class), nullable(Instant.class),
                isNull(), eq("msp-forced"), isNull(), isNull(), isNull()
        )).thenReturn(3L);

        long total = paymentService.countPaymentSummaryReport(
                null, null, "msp-ignored", List.of("SUCCESS"), null, null, null, null);

        assertEquals(3L, total);
        verify(paymentRepositoryCustom).countPaymentsWithDynamicFilters(
                eq(List.of("SUCCESS")), isNull(), nullable(Instant.class), nullable(Instant.class),
                isNull(), eq("msp-forced"), isNull(), isNull(), isNull());
    }

    @Test
    void activateMsp_callsRegistrationWithoutInvoiceIdQueryParam() {
        stubSuccessfulPutRequest();

        paymentService.activateMsp("msp-123");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodyUriSpec).uri(urlCaptor.capture());
        assertEquals(REGISTRATION_BASE_URL + "/msp/activate-license/msp-123", urlCaptor.getValue());
    }

    @Test
    void activateMsp_callsRegistrationWithInvoiceIdQueryParam() {
        stubSuccessfulPutRequest();

        paymentService.activateMsp("msp-123", "inv-456");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodyUriSpec).uri(urlCaptor.capture());
        assertEquals(REGISTRATION_BASE_URL + "/msp/activate-license/msp-123?invoiceId=inv-456", urlCaptor.getValue());
    }

    @Test
    void activateMsp_throwsBillingServiceExceptionOnWebClientError() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(
                WebClientResponseException.create(HttpStatus.BAD_REQUEST.value(), "Bad Request", null, null, null)
        );

        BillingServiceException ex = assertThrows(
                BillingServiceException.class,
                () -> paymentService.activateMsp("msp-123", "inv-456")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void activateClientAdmin_callsRegistrationActivateEndpoint() {
        stubSuccessfulPutRequest();

        paymentService.activateClientAdmin("client-123");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodyUriSpec).uri(urlCaptor.capture());
        assertEquals(REGISTRATION_BASE_URL + "/client/admin/activate-license/client-123", urlCaptor.getValue());
    }

    @Test
    void saveManualPayment_rejectsExpiredInvoice() {
        Invoice invoice = Invoice.builder()
                .id("INV-EXPIRED")
                .clientAdminId("client-1")
                .status(InvoiceStatus.EXPIRED)
                .totalAmount(100.0)
                .productSelections(List.of(ProductSelectionDto.builder()
                        .productId("p1")
                        .packageId("pkg1")
                        .licenseCount(1)
                        .pricePerLicense(100.0)
                        .build()))
                .build();
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setInvoiceId("INV-EXPIRED");
        request.setAmount(100.0);
        request.setDate(Instant.now());
        request.setPaymentSources(List.of());

        when(invoiceRepository.findById("INV-EXPIRED")).thenReturn(java.util.Optional.of(invoice));

        BillingServiceException ex = assertThrows(BillingServiceException.class,
                () -> paymentService.saveManualPayment(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invoice is expired", ex.getMessage());
    }

    private void stubSuperAdminContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userType(UserType.SUPER_ADMIN.name()).build()
        );
    }

    private void stubSuccessfulPutRequest() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
