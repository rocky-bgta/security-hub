package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.CouponDiscountResponseDTO;
import com.aspire.asat.billing.dto.InvoiceHistoryItemDTO;
import com.aspire.asat.billing.dto.InvoiceSummaryReportDTO;
import com.aspire.asat.billing.dto.PaymentSourceDTO;
import com.aspire.asat.billing.dto.invoice.*;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.repo.customRepo.InvoiceRepositoryCustom;
import com.aspire.asat.billing.service.CommentLogService;
import com.aspire.asat.billing.service.CouponService;
import com.aspire.asat.billing.service.InvoiceService;
import com.aspire.asat.billing.service.VatConfigurationService;
import com.aspire.asat.billing.config.InvoiceExpiryProperties;
import com.aspire.asat.billing.utils.QuickRangeResolver;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.billing.utils.file.CsvGeneratorUtil;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.util.InvoiceGenerator;
import com.aspire.asat.common.util.MoneyUtil;
import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceRepositoryCustom invoiceRepositoryCustom;
    @Autowired
    private InvoiceGenerator invoiceGenerator;
    @Autowired
    private VatConfigurationService vatService;
    @Autowired
    private FileService fileService;
    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private WebClient webClient;
    @Autowired
    private CommentLogService commentLogService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCurrentContextService userCurrentContextService;

    @Autowired
    private InvoiceExpiryProperties invoiceExpiryProperties;

    @Value("${service.registration.url}")
    private String registrationUrl;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Override
    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO) {
        String invoiceId = "INV-" + System.currentTimeMillis();

        // Always create as PENDING
        InvoiceStatus invoiceStatus = InvoiceStatus.PENDING;
        Instant paidAt = null;

        // Store payment details if provided (for reference only)
        PaymentMethodType paymentMethod = null;
        Object paymentDetails = null;
        if (requestDTO.getCompletedPayment() != null) {
            paymentMethod = requestDTO.getCompletedPayment().getPaymentMethod();
            if (paymentMethod == PaymentMethodType.BANK_TRANSFER) {
                paymentDetails = requestDTO.getCompletedPayment().getBankTransferDetails();
            } else if (paymentMethod == PaymentMethodType.CHECK_PAYMENT) {
                paymentDetails = requestDTO.getCompletedPayment().getCheckPaymentDetails();
            }
        }

        String reservedCouponId = null;
        Instant couponReservedAt = null;
        Instant couponValidUntil = null;
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(invoiceExpiryProperties.getDays(), ChronoUnit.DAYS);
        try {
            if (requestDTO.getCouponCode() != null && !requestDTO.getCouponCode().isBlank()) {
                CouponCreateResponseDTO coupon = couponService.validateForRedemption(
                        requestDTO.getCouponCode(),
                        requestDTO.getSubtotal(),
                        requestDTO.getProductSelections(),
                        requestDTO.getCountryId()
                );
                couponService.reserveCouponUsage(coupon.getId());
                reservedCouponId = coupon.getId();
                couponReservedAt = Instant.now();
                couponValidUntil = coupon.getValidUntil();
            }

            // Build Invoice model with new fields
            Invoice invoice = Invoice.builder()
                    .id(invoiceId)
                    .clientAdminId(requestDTO.getClientAdminId())
                    .mspAdminId(requestDTO.getMspAdminId())
                    .clientProductIds(requestDTO.getClientProductIds())
                    .subtotal(MoneyUtil.round(requestDTO.getSubtotal()))
                    .discountType(requestDTO.getDiscountType())
                    .discountAmount(MoneyUtil.round(requestDTO.getDiscountAmount()))
                    .discountPercentage(requestDTO.getDiscountPercentage())
                    .couponCode(requestDTO.getCouponCode())
                    .couponDiscountAmount(MoneyUtil.round(requestDTO.getCouponDiscountAmount()))
                    .couponId(reservedCouponId)
                    .couponUsageReserved(reservedCouponId != null)
                    .couponReservedAt(couponReservedAt)
                    .couponValidUntil(couponValidUntil)
                    .vatAmount(MoneyUtil.round(requestDTO.getVatAmount()))
                    .vatRate(requestDTO.getVatRate())
                    .totalAmount(MoneyUtil.round(requestDTO.getTotalAmount()))
                    .status(invoiceStatus)
                    .createdAt(createdAt)
                    .expiresAt(expiresAt)
                    .paidAt(paidAt)
                    .statusNote(requestDTO.getStatusNote())
                    .reason(requestDTO.getReason())
                    .invoicePdfLink(requestDTO.getInvoicePdfLink())
                    .clientName(
                            requestDTO.getClientName() != null
                                    ? requestDTO.getClientName()
                                    : "Client Name is Not Provided"
                    )
                    .mspName(requestDTO.getMspName())
                    .billingEmail(requestDTO.getBillingEmail())
                    .productSelections(
                            requestDTO.getProductSelections() != null
                                    ? requestDTO.getProductSelections()
                                    : List.of()
                    )
                    .countryName(requestDTO.getCountryName())
                    .countryCode(requestDTO.getCountryCode())
                    .countryId(requestDTO.getCountryId())
                    .stateName(requestDTO.getStateName())
                    .stateCode(requestDTO.getStateCode())
                    .stateId(requestDTO.getStateId())
                    .roleType(requestDTO.getRoleType())
                    .paymentMethod(paymentMethod)
                    .bankReceiptUrl(requestDTO.getBankReceiptUrl())
                    .paymentDetails(paymentDetails)
                    .build();

            invoiceRepository.save(invoice);

            log.info("Invoice created with ID: {}, status: {}, paymentMethod: {}, couponReserved: {}",
                    invoiceId, invoiceStatus, paymentMethod, reservedCouponId != null);

            // Create comment log if present in completed payment
            if (requestDTO.getCompletedPayment() != null && requestDTO.getCompletedPayment().getCommentLog() != null) {
                try {
                    CommentLogRequestDTO commentLogRequest = requestDTO.getCompletedPayment().getCommentLog();
                    commentLogRequest.setInvoiceId(invoice.getId());
                    commentLogService.createCommentLog(commentLogRequest);
                    log.info("Comment log created successfully for invoice ID: {}", invoice.getId());
                } catch (Exception e) {
                    log.error("Failed to create comment log for invoice ID: {}. Error: {}",
                            invoice.getId(), e.getMessage(), e);
                }
            }

            return buildInvoiceResponse(invoice);
        } catch (Exception e) {
            if (reservedCouponId != null) {
                couponService.releaseCouponUsage(reservedCouponId);
            }
            throw e;
        }
    }

    private InvoiceResponseDTO buildInvoiceResponse(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .clientAdminId(invoice.getClientAdminId())
                .clientName(invoice.getClientName())
                .mspAdminId(invoice.getMspAdminId())
                .mspName(invoice.getMspName())
                .billingEmail(invoice.getBillingEmail())
                .clientProductIds(invoice.getClientProductIds())
                .subtotal(invoice.getSubtotal())
                .discountType(invoice.getDiscountType())
                .discountAmount(invoice.getDiscountAmount())
                .discountPercentage(invoice.getDiscountPercentage())
                .couponCode(invoice.getCouponCode())
                .couponId(invoice.getCouponId())
                .couponValidUntil(invoice.getCouponValidUntil())
                .couponDiscountAmount(invoice.getCouponDiscountAmount())
                .vatAmount(invoice.getVatAmount())
                .vatPercentage(invoice.getVatRate())
                .totalAmount(invoice.getTotalAmount())
                .invoicePdfLink(invoice.getInvoicePdfLink())
                .status(invoice.getStatus())
                .reason(invoice.getReason())
                .createdAt(invoice.getCreatedAt())
                .paidAt(invoice.getPaidAt())
                .expiresAt(invoice.getExpiresAt())
                .productSelections(invoice.getProductSelections())
                .countryId(invoice.getCountryId())
                .countryName(invoice.getCountryName())
                .roleType(invoice.getRoleType())
                .paymentMethod(invoice.getPaymentMethod())
                .bankReceiptUrl(invoice.getBankReceiptUrl())
                .paymentDetails(invoice.getPaymentDetails())
                .build();
    }


    @Override
    @Transactional
    public InvoiceResponseDTO updateInvoice(String id, InvoiceRequestDTO requestDTO) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        if (isCouponCodeChanged(invoice.getCouponCode(), requestDTO.getCouponCode())) {
            if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
                throw new BillingServiceException("Cannot change coupon on a paid invoice", HttpStatus.BAD_REQUEST);
            }
            handleCouponChangeOnUpdate(invoice, requestDTO);
        }

        // Update fields from request
        invoice.setClientAdminId(requestDTO.getClientAdminId());
        invoice.setMspAdminId(requestDTO.getMspAdminId());
        invoice.setClientName(requestDTO.getClientName());
        invoice.setMspName(requestDTO.getMspName());
        invoice.setBillingEmail(requestDTO.getBillingEmail());
        invoice.setClientProductIds(requestDTO.getClientProductIds());
        invoice.setSubtotal(MoneyUtil.round(requestDTO.getSubtotal()));
        invoice.setDiscountType(requestDTO.getDiscountType());
        invoice.setDiscountAmount(MoneyUtil.round(requestDTO.getDiscountAmount()));
        invoice.setDiscountPercentage(requestDTO.getDiscountPercentage());
        invoice.setCouponCode(requestDTO.getCouponCode());
        invoice.setCouponDiscountAmount(MoneyUtil.round(requestDTO.getCouponDiscountAmount()));
        invoice.setVatAmount(MoneyUtil.round(requestDTO.getVatAmount()));
        invoice.setVatRate(requestDTO.getVatRate());
        invoice.setTotalAmount(MoneyUtil.round(requestDTO.getTotalAmount()));
        invoice.setStatusNote(requestDTO.getStatusNote());
        if (requestDTO.getReason() != null) {
            invoice.setReason(requestDTO.getReason());
        }
        invoice.setInvoicePdfLink(requestDTO.getInvoicePdfLink());
        invoice.setProductSelections(requestDTO.getProductSelections());
        invoice.setCountryName(requestDTO.getCountryName());
        invoice.setCountryCode(requestDTO.getCountryCode());
        invoice.setCountryId(requestDTO.getCountryId());
        invoice.setStateName(requestDTO.getStateName());
        invoice.setStateCode(requestDTO.getStateCode());
        invoice.setStateId(requestDTO.getStateId());
        invoice.setRoleType(requestDTO.getRoleType());

        // Handle payment status update
        boolean isCompletedPayment = requestDTO.getPaymentStatus() == PaymentStatusType.COMPLETED;
        if (isCompletedPayment) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
            if (requestDTO.getCompletedPayment() != null) {
                invoice.setPaymentMethod(requestDTO.getCompletedPayment().getPaymentMethod());
                if (requestDTO.getCompletedPayment().getPaymentMethod() == PaymentMethodType.BANK_TRANSFER) {
                    invoice.setPaymentDetails(requestDTO.getCompletedPayment().getBankTransferDetails());
                } else if (requestDTO.getCompletedPayment().getPaymentMethod() == PaymentMethodType.CHECK_PAYMENT) {
                    invoice.setPaymentDetails(requestDTO.getCompletedPayment().getCheckPaymentDetails());
                }
            }
            // Update bankReceiptUrl if provided
            if (requestDTO.getBankReceiptUrl() != null) {
                invoice.setBankReceiptUrl(requestDTO.getBankReceiptUrl());
            }
        } else {
            invoice.setStatus(InvoiceStatus.PENDING);
            invoice.setPaidAt(null);
            invoice.setPaymentMethod(null);
            invoice.setBankReceiptUrl(null);
            invoice.setPaymentDetails(null);
        }

        // Save updated invoice
        invoiceRepository.save(invoice);

        return buildInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponseDTO updateInvoicePayment(String invoiceId, InvoiceUpdateRequestDto requestDTO) {
        log.info("Updating invoice payment for invoice ID: {}", invoiceId);
        
        // Fetch invoice by ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Handle payment status update if provided
        if (requestDTO.getPaymentStatus() != null) {
            if (requestDTO.getPaymentStatus() == PaymentStatusType.COMPLETED) {
                invoice.setStatus(InvoiceStatus.PENDING);
                invoice.setPaidAt(Instant.now());
                
                // Update payment method and details if completedPayment is provided
                if (requestDTO.getCompletedPayment() != null) {
                    invoice.setPaymentMethod(requestDTO.getCompletedPayment().getPaymentMethod());
                    if (requestDTO.getCompletedPayment().getPaymentMethod() == PaymentMethodType.BANK_TRANSFER) {
                        invoice.setPaymentDetails(requestDTO.getCompletedPayment().getBankTransferDetails());
                    } else if (requestDTO.getCompletedPayment().getPaymentMethod() == PaymentMethodType.CHECK_PAYMENT) {
                        invoice.setPaymentDetails(requestDTO.getCompletedPayment().getCheckPaymentDetails());
                    }
                    
                    // Create comment log if present in completedPayment
                    if (requestDTO.getCompletedPayment().getCommentLog() != null) {
                        try {
                            CommentLogRequestDTO commentLogRequest = requestDTO.getCompletedPayment().getCommentLog();
                            commentLogRequest.setInvoiceId(invoice.getId());
                            commentLogService.createCommentLog(commentLogRequest);
                            log.info("Comment log created successfully for invoice ID: {}", invoice.getId());
                        } catch (Exception e) {
                            log.error("Failed to create comment log for invoice ID: {}. Error: {}", 
                                    invoice.getId(), e.getMessage(), e);
                            // Don't fail invoice update if comment log creation fails
                        }
                    }
                }
                // Update bankReceiptUrl if provided
                if (requestDTO.getBankReceiptUrl() != null) {
                    invoice.setBankReceiptUrl(requestDTO.getBankReceiptUrl());
                }
            } else if (requestDTO.getPaymentStatus() == PaymentStatusType.PENDING) {
                invoice.setStatus(InvoiceStatus.PENDING);
                invoice.setPaidAt(null);
                invoice.setPaymentMethod(null);
                invoice.setBankReceiptUrl(null);
                invoice.setPaymentDetails(null);
            }
        } else if (requestDTO.getCompletedPayment() != null) {
            // If only completedPayment is provided (without paymentStatus), update payment details without changing status
            invoice.setPaymentMethod(requestDTO.getCompletedPayment().getPaymentMethod());
            if (requestDTO.getCompletedPayment().getPaymentMethod() == PaymentMethodType.BANK_TRANSFER) {
                invoice.setPaymentDetails(requestDTO.getCompletedPayment().getBankTransferDetails());
            } else if (requestDTO.getCompletedPayment().getPaymentMethod() == PaymentMethodType.CHECK_PAYMENT) {
                invoice.setPaymentDetails(requestDTO.getCompletedPayment().getCheckPaymentDetails());
            }
            
            // Update bankReceiptUrl if provided
            if (requestDTO.getBankReceiptUrl() != null) {
                invoice.setBankReceiptUrl(requestDTO.getBankReceiptUrl());
            }
            
            // Create comment log if present in completedPayment
            if (requestDTO.getCompletedPayment().getCommentLog() != null) {
                try {
                    CommentLogRequestDTO commentLogRequest = requestDTO.getCompletedPayment().getCommentLog();
                    commentLogRequest.setInvoiceId(invoice.getId());
                    commentLogService.createCommentLog(commentLogRequest);
                    log.info("Comment log created successfully for invoice ID: {}", invoice.getId());
                } catch (Exception e) {
                    log.error("Failed to create comment log for invoice ID: {}. Error: {}", 
                            invoice.getId(), e.getMessage(), e);
                    // Don't fail invoice update if comment log creation fails
                }
            }
        }

        // Save updated invoice
        invoiceRepository.save(invoice);
        log.info("Invoice payment updated successfully for invoice ID: {}, status: {}", invoiceId, invoice.getStatus());

        return buildInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponseDTO getInvoiceById(String id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        // Use stored VAT rate if available, otherwise try to get from VAT service
        double vatPercentage = 0.0;
        if (invoice.getVatRate() > 0) {
            // Use stored VAT rate from invoice creation (preserves historical accuracy)
            vatPercentage = invoice.getVatRate();
            log.debug("Using stored VAT rate: {}% for invoice ID: {}", vatPercentage, invoice.getId());
        } else {
            // Fallback: Try to get from VAT service (for backward compatibility with old invoices)
            try {
                vatPercentage = vatService.getVatRate(invoice.getCountryId(), invoice.getStateId());
                log.debug("Using VAT rate from service: {}% for invoice ID: {}", vatPercentage, invoice.getId());
            } catch (Exception e) {
                log.warn("Could not determine VAT rate for invoice ID: {}, countryId: {}, stateId: {}. Reason: {}",
                        invoice.getId(), invoice.getCountryId(), invoice.getStateId(), e.getMessage());
            }
        }

        InvoiceResponseDTO response = buildInvoiceResponse(invoice);
        response.setVatPercentage(vatPercentage);
        return response;
    }




    @Override
    public List<InvoiceResponseDTO> getInvoicesByClientId(String clientId, int offset, int limit) {
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
        
        // Convert offset (page number) to actual page for PageRequest
        // offset=0 → page 0, offset=1 → page 1, offset=2 → page 2
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Invoice> invoicesPage = invoiceRepository.findByClientAdminId(clientId, pageable);

        return invoicesPage.getContent().stream()
                .map(this::buildInvoiceResponse)
                .toList();
    }

    @Override
    public List<InvoiceResponseDTO> getInvoices(String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType, String startDate, String endDate, String search, String productId, int offset, int limit) {
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

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set mspId from Current Context whether UserType
        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        // Parse dates
        Instant startDateParsed = parseInstant(startDate);
        Instant endDateParsed = parseInstant(endDate);

        // Convert offset (page number) to actual skip count for pagination
        // offset=0 → page 0 → skip 0, take limit
        // offset=1 → page 1 → skip limit, take limit
        // offset=2 → page 2 → skip 2*limit, take limit
        int skip = offset * limit;
        
        // Use custom repository for dynamic filtering with offset-based pagination
        List<Invoice> invoices = invoiceRepositoryCustom.findInvoicesWithDynamicFilters(
                clientId, mspId, countryId, statuses, roleType, startDateParsed, endDateParsed, search, productId, skip, limit);

        return invoices.stream()
                .map(this::buildInvoiceResponse)
                .toList();
    }

    @Override
    public long countInvoices(String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType, String startDate, String endDate, String search, String productId) {
        // Parse dates
        Instant startDateParsed = parseInstant(startDate);
        Instant endDateParsed = parseInstant(endDate);

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set mspId from Current Context whether UserType
        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        // Use custom repository for dynamic filtering
        return invoiceRepositoryCustom.countInvoicesWithDynamicFilters(
                clientId, mspId, countryId, statuses, roleType, startDateParsed, endDateParsed, search, productId);
    }

    private Instant parseInstant(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return LocalDate.parse(str).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", str, e);
            return null;
        }
    }



    @Override
    @Transactional
    public InvoiceResponseDTO applyCouponToInvoice(String invoiceId, ApplyCouponRequestDTO request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        assertInvoiceEligibleForCouponApply(invoice);

        String couponCode = request.getCouponCode().trim();
        String reservedCouponId = null;
        try {
            CouponCreateResponseDTO coupon = couponService.validateForRedemption(
                    couponCode,
                    invoice.getSubtotal(),
                    invoice.getProductSelections(),
                    invoice.getCountryId()
            );
            CouponDiscountResponseDTO discountResult = couponService.calculateCouponDiscount(
                    invoice.getProductSelections(),
                    invoice.getCountryId(),
                    coupon
            );
            couponService.reserveCouponUsage(coupon.getId());
            reservedCouponId = coupon.getId();

            invoice.setCouponCode(couponCode);
            invoice.setCouponId(coupon.getId());
            invoice.setCouponUsageReserved(true);
            invoice.setCouponReservedAt(Instant.now());
            invoice.setCouponValidUntil(coupon.getValidUntil());
            invoice.setCouponDiscountAmount(MoneyUtil.round(discountResult.getDiscountAmount()));
            recalculateInvoiceTotals(invoice);

            log.info("Coupon {} applied to invoice {}", couponCode, invoiceId);
            return finalizePrePaymentInvoiceUpdate(invoice, invoiceId, request.isSendEmail());
        } catch (Exception e) {
            if (reservedCouponId != null) {
                couponService.releaseCouponUsage(reservedCouponId);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public InvoiceResponseDTO applyDiscountToInvoice(String invoiceId, ApplyDiscountRequestDTO request) {
        assertSuperAdminOrSystemUser();

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        assertInvoiceEligibleForPrePaymentModification(invoice);
        assertNoExistingInvoiceDiscount(invoice);
        validateApplyDiscountRequest(request);

        invoice.setDiscountType(request.getDiscountType());
        if (request.getDiscountType() == DiscountType.PERCENTAGE) {
            invoice.setDiscountPercentage(request.getDiscountPercentage());
            invoice.setDiscountAmount(0);
        } else {
            invoice.setDiscountAmount(MoneyUtil.round(request.getDiscountAmount()));
            invoice.setDiscountPercentage(0);
        }

        if (request.getReason() != null && !request.getReason().isBlank()) {
            String existingNote = invoice.getStatusNote();
            String reasonNote = "Discount applied: " + request.getReason().trim();
            invoice.setStatusNote(
                    existingNote == null || existingNote.isBlank()
                            ? reasonNote
                            : existingNote + " | " + reasonNote
            );
        }

        recalculateInvoiceTotals(invoice);

        log.info("Discount applied to invoice {} (type: {})", invoiceId, request.getDiscountType());
        return finalizePrePaymentInvoiceUpdate(invoice, invoiceId, request.isSendEmail());
    }

    private void assertSuperAdminOrSystemUser() {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();
        if (!UserType.SUPER_ADMIN.name().equals(userType)
                && !UserType.ASPIRE_ADMIN.name().equals(userType)
                && !UserType.SYSTEM_USER.name().equals(userType)) {
            throw new BillingServiceException(
                    "Only SUPER_ADMIN, ASPIRE_ADMIN, or SYSTEM_USER can apply invoice discount",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private void validateApplyDiscountRequest(ApplyDiscountRequestDTO request) {
        if (request.getDiscountType() == null) {
            throw new BillingServiceException("discountType is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getDiscountType() == DiscountType.PERCENTAGE) {
            if (request.getDiscountPercentage() == null || request.getDiscountPercentage() <= 0) {
                throw new BillingServiceException(
                        "discountPercentage must be greater than 0 for PERCENTAGE discount",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (request.getDiscountPercentage() > 100) {
                throw new BillingServiceException(
                        "discountPercentage cannot exceed 100",
                        HttpStatus.BAD_REQUEST
                );
            }
        } else if (request.getDiscountType() == DiscountType.FLAT) {
            if (request.getDiscountAmount() == null || request.getDiscountAmount() <= 0) {
                throw new BillingServiceException(
                        "discountAmount must be greater than 0 for FLAT discount",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    private void assertNoExistingInvoiceDiscount(Invoice invoice) {
        if (invoice.getDiscountAmount() > 0 || invoice.getDiscountPercentage() > 0) {
            throw new BillingServiceException(
                    "Invoice already has a discount applied",
                    HttpStatus.CONFLICT
            );
        }
    }

    private InvoiceResponseDTO finalizePrePaymentInvoiceUpdate(Invoice invoice, String invoiceId, boolean sendEmail) {
        if (InvoiceStatus.ON_PROGRESS.equals(invoice.getStatus())) {
            invoice.setStatus(InvoiceStatus.PENDING);
        }
        invoiceRepository.save(invoice);
        if (sendEmail) {
            scheduleSendInvoiceEmailAfterCommit(invoiceId);
        }
        return buildInvoiceResponse(invoice);
    }

    /**
     * Sends invoice email after the DB transaction commits so S3/email failures do not roll back
     * invoice or coupon updates (coupon reserve uses a separate atomic Mongo operation).
     */
    private void scheduleSendInvoiceEmailAfterCommit(String invoiceId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        sendInvoiceEmail(invoiceId);
                    } catch (Exception e) {
                        log.error("Invoice saved but email failed for invoice ID: {}", invoiceId, e);
                    }
                }
            });
            return;
        }
        sendInvoiceEmail(invoiceId);
    }

    private void recalculateInvoiceTotals(Invoice invoice) {
        double baseAfterCoupon = MoneyUtil.round(invoice.getSubtotal() - invoice.getCouponDiscountAmount());
        double discountAmount;

        if (invoice.getDiscountType() == DiscountType.PERCENTAGE
                || (invoice.getDiscountType() == null && invoice.getDiscountPercentage() > 0)) {
            discountAmount = MoneyUtil.round(baseAfterCoupon * (invoice.getDiscountPercentage() / 100.0));
        } else if (invoice.getDiscountType() == DiscountType.FLAT) {
            discountAmount = MoneyUtil.round(Math.min(invoice.getDiscountAmount(), baseAfterCoupon));
            if (baseAfterCoupon > 0) {
                invoice.setDiscountPercentage((discountAmount / baseAfterCoupon) * 100.0);
            }
        } else if (invoice.getDiscountAmount() > 0) {
            discountAmount = MoneyUtil.round(Math.min(invoice.getDiscountAmount(), baseAfterCoupon));
        } else {
            discountAmount = 0;
        }

        double discountedSubtotal = MoneyUtil.round(Math.max(0, baseAfterCoupon - discountAmount));
        double vatAmount = discountedSubtotal <= 0
                ? 0
                : MoneyUtil.round(discountedSubtotal * (invoice.getVatRate() / 100.0));
        double totalAmount = MoneyUtil.round(discountedSubtotal + vatAmount);

        invoice.setDiscountAmount(discountAmount);
        invoice.setVatAmount(vatAmount);
        invoice.setTotalAmount(totalAmount);
    }

    private void assertInvoiceEligibleForCouponApply(Invoice invoice) {
        assertInvoiceEligibleForPrePaymentModification(invoice);
        if (invoice.getCouponCode() != null && !invoice.getCouponCode().isBlank()) {
            throw new BillingServiceException(
                    "Invoice already has a coupon applied: " + invoice.getCouponCode(),
                    HttpStatus.CONFLICT
            );
        }
        if (invoice.getProductSelections() == null || invoice.getProductSelections().isEmpty()) {
            throw new BillingServiceException(
                    "Invoice is missing product selections required for coupon validation",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (invoice.getCountryId() == null || invoice.getCountryId().isBlank()) {
            throw new BillingServiceException(
                    "Invoice is missing country ID required for coupon validation",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void assertInvoiceEligibleForPrePaymentModification(Invoice invoice) {
        if (InvoiceStatus.PAID.equals(invoice.getStatus())
                || InvoiceStatus.CANCELLED.equals(invoice.getStatus())
                || InvoiceStatus.EXPIRED.equals(invoice.getStatus())) {
            throw new BillingServiceException(
                    "Cannot modify invoice with status: " + invoice.getStatus(),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (!InvoiceStatus.PENDING.equals(invoice.getStatus())
                && !InvoiceStatus.ON_PROGRESS.equals(invoice.getStatus())) {
            throw new BillingServiceException(
                    "Invoice can only be modified when status is PENDING or ON_PROGRESS",
                    HttpStatus.BAD_REQUEST
            );
        }
        boolean hasSuccessfulPayment = paymentRepository.findByInvoiceId(invoice.getId()).stream()
                .anyMatch(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()));
        if (hasSuccessfulPayment) {
            throw new BillingServiceException(
                    "Cannot modify an invoice with successful payments",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Override
    @Transactional
    public void deleteInvoice(String id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        releaseCouponReservationIfUnpaid(invoice);
        invoiceRepository.delete(invoice);
    }

    private void releaseCouponReservationIfUnpaid(Invoice invoice) {
        if (invoice.isCouponUsageReserved()
                && invoice.getCouponId() != null
                && !InvoiceStatus.PAID.equals(invoice.getStatus())) {
            couponService.releaseCouponUsage(invoice.getCouponId());
            invoice.setCouponUsageReserved(false);
            invoice.setCouponId(null);
            invoice.setCouponReservedAt(null);
            invoice.setCouponValidUntil(null);
        }
    }

    private boolean isCouponCodeChanged(String existingCode, String newCode) {
        String normalizedExisting = existingCode == null || existingCode.isBlank() ? null : existingCode.trim();
        String normalizedNew = newCode == null || newCode.isBlank() ? null : newCode.trim();
        return !Objects.equals(normalizedExisting, normalizedNew);
    }

    private void handleCouponChangeOnUpdate(Invoice invoice, InvoiceRequestDTO requestDTO) {
        if (invoice.isCouponUsageReserved() && invoice.getCouponId() != null) {
            couponService.releaseCouponUsage(invoice.getCouponId());
            invoice.setCouponUsageReserved(false);
            invoice.setCouponId(null);
            invoice.setCouponReservedAt(null);
            invoice.setCouponValidUntil(null);
        }

        String newCouponCode = requestDTO.getCouponCode();
        if (newCouponCode != null && !newCouponCode.isBlank()) {
            CouponCreateResponseDTO coupon = couponService.validateForRedemption(
                    newCouponCode,
                    requestDTO.getSubtotal(),
                    requestDTO.getProductSelections(),
                    requestDTO.getCountryId()
            );
            couponService.reserveCouponUsage(coupon.getId());
            invoice.setCouponId(coupon.getId());
            invoice.setCouponUsageReserved(true);
            invoice.setCouponReservedAt(Instant.now());
            invoice.setCouponValidUntil(coupon.getValidUntil());
        }
    }

    @Override
    public byte[] downloadInvoicePdf(String invoiceId) {
        // Get invoice by ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Prepare data for PDF generation
        String clientName = invoice.getClientName() != null ? invoice.getClientName() : "Client Name Not Provided";
        double subtotal = invoice.getSubtotal();
        double discount = invoice.getDiscountAmount();
        double vatPercentage = calculateVatPercentage(invoice.getVatAmount(), subtotal, discount);
        double vat = invoice.getVatAmount();
        double total = invoice.getTotalAmount();
        Instant createdAt = invoice.getCreatedAt() != null ? invoice.getCreatedAt() : Instant.now();

        // Convert billing InvoiceStatus to common InvoiceStatus
        com.aspire.asat.common.enums.InvoiceStatus commonStatus = convertToCommonInvoiceStatus(
                invoice.getStatus() != null ? invoice.getStatus() : InvoiceStatus.PENDING);

        // Convert billing ProductSelectionDto to common ProductSelectionDto
        List<com.aspire.asat.common.dto.ProductSelectionDto> commonProductSelections =
                invoice.getProductSelections() != null
                        ? invoice.getProductSelections().stream()
                                .map(this::convertToCommonProductSelectionDto)
                                .toList()
                        : List.of();

        double couponDiscount = invoice.getCouponDiscountAmount();

        // Generate PDF using InvoiceGenerator
        ByteArrayOutputStream pdfStream = invoiceGenerator.generateInvoicePdf(
                invoiceId,
                clientName,
                subtotal,
                couponDiscount,
                discount,
                vatPercentage,
                vat,
                total,
                createdAt,
                commonProductSelections,
                commonStatus,
                invoice.getExpiresAt()
        );

        if (pdfStream == null) {
            throw new BillingServiceException(
                    "Failed to generate invoice PDF for invoice ID: " + invoiceId,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return pdfStream.toByteArray();
    }

    @Override
    public byte[] downloadInvoiceCsv(String invoiceId) {
        // Get invoice by ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Prepare data for CSV generation
        String clientName = invoice.getClientName() != null ? invoice.getClientName() : "Client Name Not Provided";
        String mspName = invoice.getMspName();
        Instant createdAt = invoice.getCreatedAt() != null ? invoice.getCreatedAt() : Instant.now();
        Instant paidAt = invoice.getPaidAt();
        String status = invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING";
        double subtotal = invoice.getSubtotal();
        double discountAmount = invoice.getDiscountAmount();
        double discountPercentage = invoice.getDiscountPercentage();
        String discountType = invoice.getDiscountType() != null ? invoice.getDiscountType().name() : null;
        double vatAmount = invoice.getVatAmount();
        double totalAmount = invoice.getTotalAmount();
        List<ProductSelectionDto> productSelections = invoice.getProductSelections() != null 
                ? invoice.getProductSelections() 
                : List.of();

        // Generate CSV using CsvGeneratorUtil
        ByteArrayOutputStream csvStream = CsvGeneratorUtil.generateInvoiceCsv(
                invoiceId,
                clientName,
                mspName,
                createdAt,
                paidAt,
                status,
                subtotal,
                discountAmount,
                discountPercentage,
                discountType,
                vatAmount,
                totalAmount,
                productSelections
        );

        return csvStream.toByteArray();
    }

    @Override
    public void sendInvoiceEmail(String invoiceId) {
        log.info("Sending invoice email for invoice ID: {}", invoiceId);

        // Get invoice by ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Get client admin email from registration service
        String clientAdminEmail = getClientAdminEmail(invoice.getClientAdminId());
        if (clientAdminEmail == null || clientAdminEmail.isEmpty()) {
            throw new BillingServiceException(
                    "Client admin email not found for invoice ID: " + invoiceId,
                    HttpStatus.BAD_REQUEST
            );
        }

        // Generate PDF
        byte[] pdfBytes = downloadInvoicePdf(invoiceId);

        // Upload PDF to S3 via common FileService (same pattern as registration onboarding)
        String s3ObjectKey = uploadInvoicePdfToS3(pdfBytes, invoiceId);
        log.info("Invoice PDF uploaded to S3 with key: {}", s3ObjectKey);

        // Create attachment DTO
        AttachmentDto attachment = AttachmentDto.builder()
                .bucketName(s3BucketName)
                .objectKey(s3ObjectKey)
                .build();

        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        Optional<Payment> relevantPayment = payments.stream()
                .filter(payment -> "SUCCESS".equalsIgnoreCase(payment.getStatus()))
                .findFirst()
                .or(() -> payments.stream()
                        .findFirst());

        String paymentMethod = relevantPayment
                .map(this::resolvePaymentMethod)
                .orElseGet(() -> invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : "Unknown");
        Instant timestampInstant = relevantPayment
                .map(payment -> payment.getPaymentDate() != null
                        ? payment.getPaymentDate()
                        : (payment.getCreatedAt() != null ? payment.getCreatedAt() : Instant.now()))
                .orElseGet(() -> invoice.getPaidAt() != null
                        ? invoice.getPaidAt()
                        : (invoice.getCreatedAt() != null ? invoice.getCreatedAt() : Instant.now()));
        String timestamp = timestampInstant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Prepare template model for PAYMENT_SUCCESS (expects method + timestamp)
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("invoiceId", invoiceId);
        templateModel.put("adminName", invoice.getClientName() != null ? invoice.getClientName() : "Client");
        templateModel.put("userName", clientAdminEmail);
        templateModel.put("amount", invoice.getTotalAmount());
        templateModel.put("invoiceDate", invoice.getCreatedAt() != null
                ? invoice.getCreatedAt().toString()
                : Instant.now().toString());
        templateModel.put("method", paymentMethod);
        templateModel.put("timestamp", timestamp);

        // Send email notification via Notification service
        try {
            boolean sent = notificationClient.sendCustomChannelNotification(
                    clientAdminEmail,
                    invoice.getClientAdminId(),
                    invoice.getClientAdminId(),
                    NotificationType.PAYMENT_SUCCESS,
                    List.of(NotificationChannel.EMAIL),
                    templateModel,
                    List.of(attachment)
            );

            if (sent) {
                log.info("Invoice email sent successfully to: {}", clientAdminEmail);
            } else {
                log.warn("Failed to send invoice email to: {}", clientAdminEmail);
                throw new BillingServiceException(
                        "Failed to send invoice email",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            log.error("Error sending invoice email: {}", e.getMessage(), e);
            throw new BillingServiceException(
                    "Failed to send invoice email: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String resolvePaymentMethod(Payment payment) {
        if (payment.getPaymentSources() == null || payment.getPaymentSources().isEmpty()) {
            return "Unknown";
        }
        String method = payment.getPaymentSources().get(0).getMethod();
        return method != null && !method.isBlank() ? method : "Unknown";
    }

    private String uploadInvoicePdfToS3(byte[] pdfBytes, String invoiceId) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("invoice_" + invoiceId, ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }

            String s3Key = "invoices/" + invoiceId + "_payment.pdf";
            log.info("Uploading invoice PDF to S3 with key: {}", s3Key);

            FileUploadResponse uploadResponse = fileService.fileUpload(tempFile.getAbsolutePath(), s3Key);
            return uploadResponse.getPath();
        } catch (Exception e) {
            log.error("Failed to upload invoice PDF to S3", e);
            throw new BillingServiceException(
                    "Failed to upload invoice PDF to S3",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Get client admin email from registration service
     */
    private String getClientAdminEmail(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isEmpty()) {
            log.warn("Client admin ID is null or empty");
            return null;
        }

        try {
            String url = registrationUrl + "/client/admin/" + clientAdminId;
            log.info("Fetching client admin email from registration service: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data != null && data.containsKey("email")) {
                    String email = (String) data.get("email");
                    log.info("Retrieved client admin email: {}", email);
                    return email;
                }
            }

            log.warn("Email not found in response for client admin ID: {}", clientAdminId);
            return null;

        } catch (WebClientResponseException.NotFound e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching client admin email: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public byte[] generateInvoiceHistoryCsv(String clientId, String mspId, String countryId,
            List<InvoiceStatus> statuses, RoleType roleType, String startDate, String endDate, String search, String productId) {
        log.info("Generating invoice history CSV with filters - clientId: {}, mspId: {}, countryId: {}, statuses: {}, roleType: {}, startDate: {}, endDate: {}, search: {}, productId: {}",
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);

        List<InvoiceResponseDTO> invoices = getInvoices(
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId, 0, Integer.MAX_VALUE);
        
        // Get all invoice IDs to query payments
        List<String> invoiceIds = invoices.stream()
                .map(InvoiceResponseDTO::getId)
                .toList();
        
        // Query all payments for these invoices in batch
        Map<String, Double> invoicePaymentsMap = new HashMap<>();
        Map<String, PaymentMethodType> invoicePaymentMethodMap = new HashMap<>();
        if (!invoiceIds.isEmpty()) {
            // Query payments by invoice IDs and SUCCESS status using optimized query
            List<Payment> payments = paymentRepository.findByInvoiceIdInAndStatusSuccess(invoiceIds);
            
            // Sum payments by invoice ID
            invoicePaymentsMap = payments.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            Payment::getInvoiceId,
                            java.util.stream.Collectors.summingDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                    ));
            
            // Determine payment method from payment sources for each invoice
            for (Payment payment : payments) {
                if (payment.getInvoiceId() != null && !invoicePaymentMethodMap.containsKey(payment.getInvoiceId())) {
                    PaymentMethodType method = determinePaymentMethodFromSources(payment);
                    if (method != null) {
                        invoicePaymentMethodMap.put(payment.getInvoiceId(), method);
                    }
                }
            }
        }
        
        final Map<String, Double> finalInvoicePaymentsMap = invoicePaymentsMap;
        final Map<String, PaymentMethodType> finalInvoicePaymentMethodMap = invoicePaymentMethodMap;
        
        // Convert to InvoiceHistoryItemDTO and calculate outstanding amounts
        List<InvoiceHistoryItemDTO> historyItems = invoices.stream()
                .map(invoice -> {
                    double totalPaid = finalInvoicePaymentsMap.getOrDefault(invoice.getId(), 0.0);
                    double outstandingAmount;
                    
                    if (invoice.getStatus() == InvoiceStatus.PAID) {
                        outstandingAmount = 0.0;
                    } else if (invoice.getStatus() == InvoiceStatus.PARTIAL) {
                        outstandingAmount = Math.max(0, invoice.getTotalAmount() - totalPaid);
                    } else {
                        // For PENDING, OVERDUE, ON_PROGRESS, etc. - use full amount
                        outstandingAmount = invoice.getTotalAmount();
                    }
                    
                    // Determine payment method: prefer from payment sources, fall back to invoice field
                    PaymentMethodType paymentMethod = finalInvoicePaymentMethodMap.getOrDefault(
                            invoice.getId(), invoice.getPaymentMethod());
                    
                    return InvoiceHistoryItemDTO.builder()
                            .id(invoice.getId())
                            .invoiceNumber(invoice.getId()) // Use invoice ID as invoice number
                            .clientId(invoice.getClientAdminId())
                            .clientName(invoice.getClientName())
                            .mspId(invoice.getMspAdminId())
                            .mspName(invoice.getMspName())
                            .countryId(invoice.getCountryId())
                            .countryName(invoice.getCountryName())
                            .invoiceDate(invoice.getCreatedAt())
                            .paidDate(invoice.getPaidAt())
                            .status(invoice.getStatus())
                            .roleType(invoice.getRoleType())
                            .subtotal(invoice.getSubtotal())
                            .discountAmount(invoice.getDiscountAmount())
                            .couponDiscountAmount(invoice.getCouponDiscountAmount())
                            .vatAmount(invoice.getVatAmount())
                            .totalAmount(invoice.getTotalAmount())
                            .outstandingAmount(outstandingAmount)
                            .paymentMethod(paymentMethod)
                            .build();
                })
                .toList();
        
        // Generate CSV
        ByteArrayOutputStream csvOutput = CsvGeneratorUtil.generateInvoiceHistoryCsv(historyItems);
        return csvOutput.toByteArray();
    }

    /**
     * Convert billing module's ProductSelectionDto to common module's ProductSelectionDto
     */
    private com.aspire.asat.common.dto.ProductSelectionDto convertToCommonProductSelectionDto(ProductSelectionDto billingDto) {
        if (billingDto == null) {
            return null;
        }
        return com.aspire.asat.common.dto.ProductSelectionDto.builder()
                .productId(billingDto.getProductId())
                .packageId(billingDto.getPackageId())
                .licenseCount(billingDto.getLicenseCount())
                .pricePerLicense(billingDto.getPricePerLicense())
                .validityPeriod(billingDto.getValidityPeriod())
                .validityUnit(billingDto.getValidityUnit() != null
                        ? com.aspire.asat.common.dto.ValidityUnit.valueOf(billingDto.getValidityUnit().name())
                        : null)
                .productName(billingDto.getProductName())
                .packageName(billingDto.getPackageName())
                .build();
    }

    /**
     * Convert billing module's InvoiceStatus to common module's InvoiceStatus
     */
    private com.aspire.asat.common.enums.InvoiceStatus convertToCommonInvoiceStatus(InvoiceStatus billingStatus) {
        if (billingStatus == null) {
            return com.aspire.asat.common.enums.InvoiceStatus.PENDING;
        }
        return com.aspire.asat.common.enums.InvoiceStatus.valueOf(billingStatus.name());
    }

    /**
     * Calculate VAT percentage based on VAT amount, subtotal, and discount.
     *
     * @param vatAmount the VAT amount
     * @param subtotal  the subtotal before discount
     * @param discount  the discount amount
     * @return the VAT percentage, or 0.0 if VAT amount is zero or base amount is zero
     */
    private double calculateVatPercentage(double vatAmount, double subtotal, double discount) {
        if (vatAmount <= 0) {
            return 0.0;
        }
        double baseAmount = subtotal - discount;
        if (baseAmount <= 0) {
            return 0.0;
        }
        return (vatAmount / baseAmount) * 100.0;
    }

    @Override
    public boolean hasOnlyPendingInvoices(String clientAdminId) {
        log.info("Checking if client admin {} has only PENDING/ON_PROGRESS invoices", clientAdminId);
        
        // First check if any invoices exist for this client admin
        long totalInvoiceCount = invoiceRepository.countByClientAdminId(clientAdminId);
        
        // If no invoices exist, return false
        if (totalInvoiceCount == 0) {
            log.info("Client admin {} has no invoices. Returning false.", clientAdminId);
            return false;
        }
        
        // Check if client admin has any PAID invoices
        long paidInvoiceCount = invoiceRepository.countByClientAdminIdAndStatus(clientAdminId, InvoiceStatus.PAID);
        
        // Return true if no PAID invoices exist (all are PENDING or ON_PROGRESS), false otherwise
        boolean hasOnlyPending = paidInvoiceCount == 0;
        
        log.info("Client admin {} has {} total invoices, {} PAID invoices. Has only PENDING/ON_PROGRESS: {}", 
                clientAdminId, totalInvoiceCount, paidInvoiceCount, hasOnlyPending);
        
        return hasOnlyPending;
    }

    /**
     * Determines the PaymentMethodType from payment sources.
     * Prioritizes non-CREDIT methods (STRIPE, PAYPAL, BANK_TRANSFER, CHECK_PAYMENT).
     *
     * @param payment The payment to extract method from
     * @return PaymentMethodType or null if not determinable
     */
    private PaymentMethodType determinePaymentMethodFromSources(Payment payment) {
        if (payment == null || payment.getPaymentSources() == null || payment.getPaymentSources().isEmpty()) {
            return null;
        }
        
        for (PaymentSourceDTO source : payment.getPaymentSources()) {
            String method = source.getMethod();
            if (method == null) continue;
            
            // Skip CREDIT as it's not a payment method for CSV export
            if ("CREDIT".equalsIgnoreCase(method)) continue;
            
            // Map string method to PaymentMethodType enum
            if ("STRIPE".equalsIgnoreCase(method)) {
                return PaymentMethodType.STRIPE;
            }
            if ("PAYPAL".equalsIgnoreCase(method)) {
                return PaymentMethodType.PAYPAL;
            }
            if ("BANK_TRANSFER".equalsIgnoreCase(method)) {
                return PaymentMethodType.BANK_TRANSFER;
            }
            if ("CHECK_PAYMENT".equalsIgnoreCase(method)) {
                return PaymentMethodType.CHECK_PAYMENT;
            }
        }
        
        return null;
    }

    @Override
    public InvoiceSummaryReportDTO getInvoiceSummaryReport(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        String effectiveStart = dates[0];
        String effectiveEnd = dates[1];

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (UserType.MSP.name().equals(userContext.getUserType())) {
            mspId = userContext.getUserId();
        }

        Instant startDateParsed = parseInstant(effectiveStart);
        Instant endDateParsed = parseInstant(effectiveEnd);

        Map<InvoiceStatus, Long> countsByStatus = invoiceRepositoryCustom.countInvoicesByStatus(
                clientAdminId, mspId, null, statuses, null, startDateParsed, endDateParsed, search, null);

        long paid = countsByStatus.getOrDefault(InvoiceStatus.PAID, 0L);
        long unpaid = countsByStatus.getOrDefault(InvoiceStatus.PENDING, 0L);
        long cancelled = countsByStatus.getOrDefault(InvoiceStatus.CANCELLED, 0L);
        long onProgress = countsByStatus.getOrDefault(InvoiceStatus.ON_PROGRESS, 0L);
        long total = countsByStatus.values().stream().mapToLong(Long::longValue).sum();

        return InvoiceSummaryReportDTO.builder()
                .totalInvoiceCount(total)
                .paidInvoiceCount(paid)
                .unpaidInvoiceCount(unpaid)
                .cancelledInvoiceCount(cancelled)
                .onProgressInvoiceCount(onProgress)
                .build();
    }

    @Override
    public List<InvoiceResponseDTO> getInvoiceSummaryReportList(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange,
            int offset, int limit) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        return getInvoices(clientAdminId, mspId, null, statuses, null, dates[0], dates[1], search, null, offset, limit);
    }

    @Override
    public long countInvoiceSummaryReport(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        return countInvoices(clientAdminId, mspId, null, statuses, null, dates[0], dates[1], search, null);
    }

    @Override
    public byte[] generateInvoiceSummaryReportCsv(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        List<InvoiceResponseDTO> invoices = getInvoices(
                clientAdminId, mspId, null, statuses, null, dates[0], dates[1], search, null, 0, Integer.MAX_VALUE);
        ByteArrayOutputStream csvOutput = CsvGeneratorUtil.generateInvoiceSummaryReportCsv(invoices);
        return csvOutput.toByteArray();
    }

}
