package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.config.InvoiceExpiryProperties;
import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.CouponDiscountResponseDTO;
import com.aspire.asat.billing.dto.PaymentSourceDTO;
import com.aspire.asat.billing.dto.invoice.ApplyCouponRequestDTO;
import com.aspire.asat.billing.dto.invoice.ApplyDiscountRequestDTO;
import com.aspire.asat.billing.dto.invoice.DiscountType;
import com.aspire.asat.billing.dto.invoice.InvoiceResponseDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.CouponLimitExceededException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.repo.customRepo.InvoiceRepositoryCustom;
import com.aspire.asat.billing.service.CouponService;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.common.util.InvoiceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceRepositoryCustom invoiceRepositoryCustom;

    @Mock
    private InvoiceGenerator invoiceGenerator;

    @Mock
    private CouponService couponService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserCurrentContextService userCurrentContextService;

    @Mock
    private FileService fileService;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private InvoiceExpiryProperties invoiceExpiryProperties;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private static final String INVOICE_ID = "INV-1001";
    private static final String COUNTRY_ID = "country-1";
    private static final List<ProductSelectionDto> PRODUCT_SELECTIONS = List.of(
            ProductSelectionDto.builder()
                    .productId("prod-1")
                    .packageId("pkg-1")
                    .licenseCount(10)
                    .pricePerLicense(100.0)
                    .validityPeriod(1)
                    .build()
    );

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field registrationUrlField = InvoiceServiceImpl.class.getDeclaredField("registrationUrl");
        registrationUrlField.setAccessible(true);
        registrationUrlField.set(invoiceService, "http://registration/api/v1");
        Field s3BucketNameField = InvoiceServiceImpl.class.getDeclaredField("s3BucketName");
        s3BucketNameField.setAccessible(true);
        s3BucketNameField.set(invoiceService, "asatv2-media-bucket");
        when(paymentRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of());
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userType(UserType.SUPER_ADMIN.name()).build()
        );
        org.mockito.Mockito.lenient().when(invoiceExpiryProperties.getDays()).thenReturn(30);
    }

    @Test
    void createInvoice_setsExpiresAtFromConfiguredDays() {
        com.aspire.asat.billing.dto.invoice.InvoiceRequestDTO request =
                new com.aspire.asat.billing.dto.invoice.InvoiceRequestDTO();
        request.setClientAdminId("client-1");
        request.setClientName("Client");
        request.setSubtotal(100.0);
        request.setDiscountAmount(0.0);
        request.setVatAmount(0.0);
        request.setTotalAmount(100.0);
        request.setProductSelections(PRODUCT_SELECTIONS);
        request.setClientProductIds(List.of("cp-1"));

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponseDTO response = invoiceService.createInvoice(request);

        assertEquals(InvoiceStatus.PENDING, response.getStatus());
        assertNull(response.getReason());
        assertNotNull(response.getExpiresAt());
        assertNotNull(response.getCreatedAt());
        assertEquals(
                response.getCreatedAt().plus(30, java.time.temporal.ChronoUnit.DAYS),
                response.getExpiresAt()
        );
    }

    @Test
    void downloadInvoicePdf_usesStoredCouponDiscountAmount() throws IOException {
        Invoice invoice = Invoice.builder()
                .id("INV-1781275820401")
                .clientName("Aspire")
                .subtotal(750.0)
                .discountAmount(0.0)
                .couponDiscountAmount(75.0)
                .vatAmount(81.0)
                .totalAmount(756.0)
                .status(InvoiceStatus.PAID)
                .createdAt(Instant.parse("2026-06-12T14:50:20.402Z"))
                .productSelections(List.of())
                .build();

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        pdfStream.write(new byte[] { 0x25, 0x50, 0x44, 0x46 });

        when(invoiceRepository.findById("INV-1781275820401")).thenReturn(Optional.of(invoice));
        when(invoiceGenerator.generateInvoicePdf(
                any(),
                any(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(pdfStream);

        byte[] pdf = invoiceService.downloadInvoicePdf("INV-1781275820401");

        assertNotNull(pdf);
        verify(invoiceGenerator).generateInvoicePdf(
                eq("INV-1781275820401"),
                eq("Aspire"),
                eq(750.0),
                eq(75.0),
                eq(0.0),
                anyDouble(),
                eq(81.0),
                eq(756.0),
                eq(Instant.parse("2026-06-12T14:50:20.402Z")),
                eq(List.of()),
                eq(com.aspire.asat.common.enums.InvoiceStatus.PAID),
                isNull()
        );
    }

    @Test
    void applyCouponToInvoice_success_recalculatesTotalsAndReservesCoupon() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .discountPercentage(0)
                .discountAmount(0)
                .vatRate(10.0)
                .build();

        CouponCreateResponseDTO coupon = couponResponse("coupon-id-1", "SUMMER10");
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(couponService.validateForRedemption(eq("SUMMER10"), eq(1000.0), eq(PRODUCT_SELECTIONS), eq(COUNTRY_ID)))
                .thenReturn(coupon);
        when(couponService.calculateCouponDiscount(eq(PRODUCT_SELECTIONS), eq(COUNTRY_ID), eq(coupon)))
                .thenReturn(new CouponDiscountResponseDTO(1000.0, 100.0, 900.0, 0.0, 900.0, List.of()));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplyCouponRequestDTO request = ApplyCouponRequestDTO.builder()
                .couponCode("SUMMER10")
                .sendEmail(false)
                .build();

        InvoiceResponseDTO response = invoiceService.applyCouponToInvoice(INVOICE_ID, request);

        assertEquals("SUMMER10", response.getCouponCode());
        assertEquals("coupon-id-1", response.getCouponId());
        assertEquals(100.0, response.getCouponDiscountAmount());
        assertEquals(90.0, response.getVatAmount());
        assertEquals(990.0, response.getTotalAmount());

        ArgumentCaptor<Invoice> savedCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(savedCaptor.capture());
        Invoice saved = savedCaptor.getValue();
        assertEquals("coupon-id-1", saved.getCouponId());
        assertEquals(true, saved.isCouponUsageReserved());
        assertNotNull(saved.getCouponValidUntil());
        assertEquals(coupon.getValidUntil(), saved.getCouponValidUntil());
        assertEquals(coupon.getValidUntil(), response.getCouponValidUntil());
        verify(couponService).reserveCouponUsage("coupon-id-1");
    }

    @Test
    void applyCouponToInvoice_appliesInvoiceDiscountAfterCoupon() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .discountPercentage(5.0)
                .discountAmount(0)
                .vatRate(10.0)
                .build();

        CouponCreateResponseDTO coupon = couponResponse("coupon-id-2", "SAVE100");
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(couponService.validateForRedemption(anyString(), anyDouble(), anyList(), anyString()))
                .thenReturn(coupon);
        when(couponService.calculateCouponDiscount(anyList(), anyString(), eq(coupon)))
                .thenReturn(new CouponDiscountResponseDTO(1000.0, 100.0, 900.0, 0.0, 900.0, List.of()));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO response = invoiceService.applyCouponToInvoice(INVOICE_ID,
                ApplyCouponRequestDTO.builder().couponCode("SAVE100").build());

        assertEquals(100.0, response.getCouponDiscountAmount());
        assertEquals(45.0, response.getDiscountAmount());
        assertEquals(85.5, response.getVatAmount());
        assertEquals(940.5, response.getTotalAmount());
    }

    @Test
    void applyCouponToInvoice_rejectsWhenCouponAlreadyApplied() {
        Invoice invoice = eligibleInvoiceBuilder()
                .couponCode("EXISTING")
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(couponService, never()).reserveCouponUsage(anyString());
    }

    @Test
    void applyCouponToInvoice_rejectsPaidInvoice() {
        Invoice invoice = eligibleInvoiceBuilder()
                .status(InvoiceStatus.PAID)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void applyCouponToInvoice_rejectsCancelledInvoice() {
        Invoice invoice = eligibleInvoiceBuilder()
                .status(InvoiceStatus.CANCELLED)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void applyCouponToInvoice_rejectsWhenSuccessfulPaymentExists() {
        Invoice invoice = eligibleInvoiceBuilder().build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        Payment successPayment = new Payment();
        successPayment.setStatus("SUCCESS");
        when(paymentRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(successPayment));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(couponService, never()).reserveCouponUsage(anyString());
    }

    @Test
    void applyCouponToInvoice_releasesReservationWhenSaveFails() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .vatRate(10.0)
                .build();

        CouponCreateResponseDTO coupon = couponResponse("coupon-id-3", "SUMMER10");
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(couponService.validateForRedemption(anyString(), anyDouble(), anyList(), anyString()))
                .thenReturn(coupon);
        when(couponService.calculateCouponDiscount(anyList(), anyString(), eq(coupon)))
                .thenReturn(new CouponDiscountResponseDTO(1000.0, 100.0, 900.0, 0.0, 900.0, List.of()));
        doThrow(new RuntimeException("save failed")).when(invoiceRepository).save(any(Invoice.class));

        assertThrows(RuntimeException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));

        verify(couponService).releaseCouponUsage("coupon-id-3");
    }

    @Test
    void applyCouponToInvoice_releasesReservationWhenReserveFails() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .vatRate(10.0)
                .build();

        CouponCreateResponseDTO coupon = couponResponse("coupon-id-4", "SUMMER10");
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(couponService.validateForRedemption(anyString(), anyDouble(), anyList(), anyString()))
                .thenReturn(coupon);
        when(couponService.calculateCouponDiscount(anyList(), anyString(), eq(coupon)))
                .thenReturn(new CouponDiscountResponseDTO(1000.0, 100.0, 900.0, 0.0, 900.0, List.of()));
        doThrow(new CouponLimitExceededException("limit reached"))
                .when(couponService).reserveCouponUsage("coupon-id-4");

        assertThrows(CouponLimitExceededException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));

        verify(couponService, never()).releaseCouponUsage(anyString());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void applyCouponToInvoice_invoiceNotFound() {
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.applyCouponToInvoice(INVOICE_ID,
                        ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build()));
    }

    @Test
    void applyCouponToInvoice_resetsOnProgressToPending() {
        Invoice invoice = eligibleInvoiceBuilder()
                .status(InvoiceStatus.ON_PROGRESS)
                .subtotal(1000.0)
                .vatRate(10.0)
                .build();

        CouponCreateResponseDTO coupon = couponResponse("coupon-id-5", "SUMMER10");
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(couponService.validateForRedemption(anyString(), anyDouble(), anyList(), anyString()))
                .thenReturn(coupon);
        when(couponService.calculateCouponDiscount(anyList(), anyString(), eq(coupon)))
                .thenReturn(new CouponDiscountResponseDTO(1000.0, 100.0, 900.0, 0.0, 900.0, List.of()));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO response = invoiceService.applyCouponToInvoice(INVOICE_ID,
                ApplyCouponRequestDTO.builder().couponCode("SUMMER10").build());

        assertEquals(InvoiceStatus.PENDING, response.getStatus());
    }

    @Test
    void applyDiscountToInvoice_percentage_recalculatesTotals() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .discountAmount(0)
                .discountPercentage(0)
                .vatRate(10.0)
                .build();

        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO response = invoiceService.applyDiscountToInvoice(INVOICE_ID,
                ApplyDiscountRequestDTO.builder()
                        .discountType(DiscountType.PERCENTAGE)
                        .discountPercentage(10.0)
                        .build());

        assertEquals(DiscountType.PERCENTAGE, response.getDiscountType());
        assertEquals(100.0, response.getDiscountAmount());
        assertEquals(90.0, response.getVatAmount());
        assertEquals(990.0, response.getTotalAmount());
    }

    @Test
    void applyDiscountToInvoice_flat_recalculatesTotals() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .discountAmount(0)
                .discountPercentage(0)
                .vatRate(10.0)
                .build();

        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO response = invoiceService.applyDiscountToInvoice(INVOICE_ID,
                ApplyDiscountRequestDTO.builder()
                        .discountType(DiscountType.FLAT)
                        .discountAmount(50.0)
                        .build());

        assertEquals(DiscountType.FLAT, response.getDiscountType());
        assertEquals(50.0, response.getDiscountAmount());
        assertEquals(95.0, response.getVatAmount());
        assertEquals(1045.0, response.getTotalAmount());
    }

    @Test
    void applyDiscountToInvoice_withExistingCoupon_appliesDiscountOnPostCouponBase() {
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .couponDiscountAmount(100.0)
                .discountAmount(0)
                .discountPercentage(0)
                .vatRate(10.0)
                .build();

        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO response = invoiceService.applyDiscountToInvoice(INVOICE_ID,
                ApplyDiscountRequestDTO.builder()
                        .discountType(DiscountType.PERCENTAGE)
                        .discountPercentage(5.0)
                        .build());

        assertEquals(45.0, response.getDiscountAmount());
        assertEquals(85.5, response.getVatAmount());
        assertEquals(940.5, response.getTotalAmount());
    }

    @Test
    void applyDiscountToInvoice_rejectsWhenDiscountAlreadyApplied() {
        Invoice invoice = eligibleInvoiceBuilder()
                .discountPercentage(5.0)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyDiscountToInvoice(INVOICE_ID,
                        ApplyDiscountRequestDTO.builder()
                                .discountType(DiscountType.PERCENTAGE)
                                .discountPercentage(10.0)
                                .build()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void applyDiscountToInvoice_allowsAspireAdmin() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userType(UserType.ASPIRE_ADMIN.name()).build()
        );
        Invoice invoice = eligibleInvoiceBuilder()
                .subtotal(1000.0)
                .vatRate(10.0)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO response = invoiceService.applyDiscountToInvoice(INVOICE_ID,
                ApplyDiscountRequestDTO.builder()
                        .discountType(DiscountType.PERCENTAGE)
                        .discountPercentage(10.0)
                        .build());

        assertEquals(990.0, response.getTotalAmount());
    }

    @Test
    void applyDiscountToInvoice_rejectsNonAdminUser() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userType(UserType.CLIENT_ADMIN.name()).build()
        );

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyDiscountToInvoice(INVOICE_ID,
                        ApplyDiscountRequestDTO.builder()
                                .discountType(DiscountType.PERCENTAGE)
                                .discountPercentage(10.0)
                                .build()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void applyDiscountToInvoice_rejectsPaidInvoice() {
        Invoice invoice = eligibleInvoiceBuilder()
                .status(InvoiceStatus.PAID)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyDiscountToInvoice(INVOICE_ID,
                        ApplyDiscountRequestDTO.builder()
                                .discountType(DiscountType.PERCENTAGE)
                                .discountPercentage(10.0)
                                .build()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void applyDiscountToInvoice_rejectsWhenSuccessfulPaymentExists() {
        Invoice invoice = eligibleInvoiceBuilder().build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        Payment successPayment = new Payment();
        successPayment.setStatus("SUCCESS");
        when(paymentRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(successPayment));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.applyDiscountToInvoice(INVOICE_ID,
                        ApplyDiscountRequestDTO.builder()
                                .discountType(DiscountType.PERCENTAGE)
                                .discountPercentage(10.0)
                                .build()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void sendInvoiceEmail_uploadsPdfAndSendsNotification() throws IOException {
        String clientAdminId = "admin-1";
        Invoice invoice = eligibleInvoiceBuilder()
                .clientAdminId(clientAdminId)
                .clientName("Test Client")
                .subtotal(1000.0)
                .vatAmount(100.0)
                .totalAmount(1100.0)
                .createdAt(Instant.parse("2026-06-12T14:50:20.402Z"))
                .build();

        PaymentSourceDTO paymentSource = new PaymentSourceDTO();
        paymentSource.setMethod("STRIPE");
        Payment successPayment = new Payment();
        successPayment.setStatus("SUCCESS");
        successPayment.setPaymentDate(Instant.parse("2026-06-12T15:00:00Z"));
        successPayment.setPaymentSources(List.of(paymentSource));

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        pdfStream.write(new byte[] { 0x25, 0x50, 0x44, 0x46 });

        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(successPayment));
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(
                Mono.just(Map.of("data", Map.of("email", "admin@test.com")))
        );
        when(invoiceGenerator.generateInvoicePdf(
                any(), any(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any(), anyList(), any(), any()
        )).thenReturn(pdfStream);
        when(fileService.fileUpload(anyString(), eq("invoices/" + INVOICE_ID + "_payment.pdf")))
                .thenReturn(FileUploadResponse.builder()
                        .provider("s3")
                        .bucketOrContainer("asatv2-media-bucket")
                        .path("invoices/" + INVOICE_ID + "_payment.pdf")
                        .build());
        when(notificationClient.sendCustomChannelNotification(
                anyString(), anyString(), anyString(), any(), anyList(), any(), anyList()
        )).thenReturn(true);

        invoiceService.sendInvoiceEmail(INVOICE_ID);

        verify(fileService).fileUpload(
                anyString(),
                eq("invoices/" + INVOICE_ID + "_payment.pdf")
        );
        ArgumentCaptor<List<AttachmentDto>> attachmentCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> templateModelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationClient).sendCustomChannelNotification(
                eq("admin@test.com"),
                eq(clientAdminId),
                eq(clientAdminId),
                eq(NotificationType.PAYMENT_SUCCESS),
                eq(List.of(NotificationChannel.EMAIL)),
                templateModelCaptor.capture(),
                attachmentCaptor.capture()
        );
        AttachmentDto attachment = attachmentCaptor.getValue().get(0);
        assertEquals("asatv2-media-bucket", attachment.getBucketName());
        assertEquals("invoices/" + INVOICE_ID + "_payment.pdf", attachment.getObjectKey());
        Map<String, Object> templateModel = templateModelCaptor.getValue();
        assertEquals("STRIPE", templateModel.get("method"));
        assertNotNull(templateModel.get("timestamp"));
        assertEquals(1100.0, templateModel.get("amount"));
    }

    @Test
    void sendInvoiceEmail_propagatesUploadFailure() throws IOException {
        Invoice invoice = eligibleInvoiceBuilder()
                .clientAdminId("admin-1")
                .subtotal(1000.0)
                .vatAmount(100.0)
                .totalAmount(1100.0)
                .build();

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        pdfStream.write(new byte[] { 0x25, 0x50, 0x44, 0x46 });

        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(
                Mono.just(Map.of("data", Map.of("email", "admin@test.com")))
        );
        when(invoiceGenerator.generateInvoicePdf(
                any(), any(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any(), anyList(), any(), any()
        )).thenReturn(pdfStream);
        when(fileService.fileUpload(anyString(), anyString()))
                .thenThrow(new RuntimeException("S3 upload failed"));

        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                invoiceService.sendInvoiceEmail(INVOICE_ID));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertEquals("Failed to upload invoice PDF to S3", ex.getMessage());
        verify(notificationClient, never()).sendCustomChannelNotification(
                anyString(), anyString(), anyString(), any(), anyList(), any(), anyList()
        );
    }

    @Test
    void getInvoiceById_returnsCouponIdWhenPresent() {
        Invoice invoice = eligibleInvoiceBuilder()
                .couponCode("SUMMER10")
                .couponId("coupon-id-1")
                .vatRate(10.0)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        InvoiceResponseDTO response = invoiceService.getInvoiceById(INVOICE_ID);

        assertEquals("coupon-id-1", response.getCouponId());
        assertEquals("SUMMER10", response.getCouponCode());
    }

    @Test
    void getInvoiceById_returnsNullCouponIdWhenNoCoupon() {
        Invoice invoice = eligibleInvoiceBuilder()
                .vatRate(10.0)
                .build();
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        InvoiceResponseDTO response = invoiceService.getInvoiceById(INVOICE_ID);

        assertNull(response.getCouponId());
        assertNull(response.getCouponCode());
    }

    @Test
    void getInvoices_returnsCouponIdInListItems() {
        Invoice withCoupon = eligibleInvoiceBuilder()
                .id("INV-2001")
                .couponCode("SAVE10")
                .couponId("coupon-id-2")
                .build();
        Invoice withoutCoupon = eligibleInvoiceBuilder()
                .id("INV-2002")
                .build();
        when(invoiceRepositoryCustom.findInvoicesWithDynamicFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), isNull(), eq(0), eq(10)))
                .thenReturn(List.of(withCoupon, withoutCoupon));

        List<InvoiceResponseDTO> responses = invoiceService.getInvoices(
                null, null, null, null, null, null, null, null, null, 0, 10);

        assertEquals(2, responses.size());
        assertEquals("coupon-id-2", responses.get(0).getCouponId());
        assertNull(responses.get(1).getCouponId());
    }

    @Test
    void getInvoices_withProductId_forwardsFilterToRepository() {
        when(invoiceRepositoryCustom.findInvoicesWithDynamicFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), eq("prod-1"), eq(0), eq(10)))
                .thenReturn(List.of());

        invoiceService.getInvoices(
                "client-1", null, null, null, null, null, null, null, "prod-1", 0, 10);

        verify(invoiceRepositoryCustom).findInvoicesWithDynamicFilters(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("prod-1"), eq(0), eq(10));
    }

    @Test
    void getInvoices_withoutProductId_preservesExistingBehavior() {
        when(invoiceRepositoryCustom.findInvoicesWithDynamicFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), isNull(), eq(0), eq(10)))
                .thenReturn(List.of());

        invoiceService.getInvoices(
                null, null, null, null, null, null, null, null, null, 0, 10);

        verify(invoiceRepositoryCustom).findInvoicesWithDynamicFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), eq(0), eq(10));
    }

    private Invoice.InvoiceBuilder eligibleInvoiceBuilder() {
        return Invoice.builder()
                .id(INVOICE_ID)
                .status(InvoiceStatus.PENDING)
                .countryId(COUNTRY_ID)
                .productSelections(PRODUCT_SELECTIONS);
    }

    private CouponCreateResponseDTO couponResponse(String id, String code) {
        CouponCreateResponseDTO coupon = new CouponCreateResponseDTO();
        coupon.setId(id);
        coupon.setCode(code);
        coupon.setValidUntil(Instant.parse("2026-06-10T23:59:59Z"));
        return coupon;
    }
}
