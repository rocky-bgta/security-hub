package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.common.util.MoneyUtil;
import com.aspire.asat.billing.dto.ClientInfoRequestDTO;
import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.CouponDiscountResponseDTO;
import com.aspire.asat.billing.dto.CreditTransactionCreateDTO;
import com.aspire.asat.billing.dto.CreditTransactionUpdateRequestDTO;
import com.aspire.asat.billing.dto.DiscountedItemDTO;
import com.aspire.asat.billing.dto.GatewayResponseDTO;
import com.aspire.asat.billing.dto.PaymentGatewayRequestDTO;
import com.aspire.asat.billing.dto.PaymentHistoryItemDTO;
import com.aspire.asat.billing.dto.PaymentHistorySummaryDTO;
import com.aspire.asat.billing.dto.PaymentRequestDTO;
import com.aspire.asat.billing.dto.PaymentResponseDTO;
import com.aspire.asat.billing.dto.PaymentSourceDTO;
import com.aspire.asat.billing.dto.PaymentSummaryReportDTO;
import com.aspire.asat.billing.dto.ProductRestrictionDTO;
import com.aspire.asat.billing.dto.TransactionStatus;
import com.aspire.asat.billing.dto.UsedCreditPaymentRequestDTO;
import com.aspire.asat.billing.dto.WebhookResponseDTO;
import com.aspire.asat.billing.dto.invoice.BankTransferDetailsDto;
import com.aspire.asat.billing.dto.invoice.CheckPaymentDetailsDto;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.PaymentMethodType;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.dto.payment.request.PaymentType;
import com.aspire.asat.billing.dto.payment.response.PaymentDetailsDTO;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.CouponValidationException;
import com.aspire.asat.billing.exception.CreditAccountInactiveException;
import com.aspire.asat.billing.exception.InsufficientCreditBalanceException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.ClientInfo;
import com.aspire.asat.billing.model.Credit;
import com.aspire.asat.billing.model.CreditTransaction;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.ClientInfoRepository;
import com.aspire.asat.billing.repo.CreditRepository;
import com.aspire.asat.billing.repo.CreditTransactionRepository;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.repo.customRepo.PaymentRepositoryCustom;
import com.aspire.asat.billing.repo.customRepo.PaymentStatusAggregate;
import com.aspire.asat.billing.repo.customRepo.PaymentSummaryResult;
import com.aspire.asat.billing.service.CommissionRateService;
import com.aspire.asat.billing.service.CouponService;
import com.aspire.asat.billing.service.CreditService;
import com.aspire.asat.billing.service.InvoiceService;
import com.aspire.asat.billing.service.PaymentService;
import com.aspire.asat.billing.utils.PaymentNotificationUtil;
import com.aspire.asat.billing.utils.QuickRangeResolver;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.billing.utils.file.AzureBlobUploader;
import com.aspire.asat.billing.utils.file.AzureInvoiceUploader;
import com.aspire.asat.billing.utils.file.CsvGeneratorUtil;
import com.aspire.asat.billing.utils.file.ExcelGeneratorUtil;
import com.aspire.asat.billing.utils.file.PdfGeneratorService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ClientInfoRepository clientInfoRepository;
    private final PaymentRepository paymentRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final AzureBlobUploader azureBlobUploader;
    private final CouponService couponService;
    private final CreditRepository creditRepository;
    private final CreditService creditService;
    private final CommissionRateService commissionRateService;
    private final CreditTransactionRepository creditTransactionRepository;
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final WebClient webClient;
    private final PaymentRepositoryCustom paymentRepositoryCustom;
    @Autowired
    private AzureInvoiceUploader azureInvoiceUploader;
    private final ObjectMapper objectMapper;
    private final PaymentNotificationUtil paymentNotificationUtil;
    private final UserCurrentContextService userCurrentContextService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;
    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${paypal.client-id}")
    private String paypalClientId;
    @Value("${paypal.secret}")
    private String paypalSecret;
    @Value("${paypal.mode}")
    private String paypalMode;
    @Value("${service.registration.url}")
    private String registrationUrl;
    @Value("${service.frontEnd.url}")
    private String frontEndUrl;

    /**
     * This method is used to save client information.
     *
     * @param clientInfoRequestDTO
     * @return
     * @author: Mahadi Hasan Joy
     * @since: 2023-04-10
     */
    @Override
    public ClientInfo saveClientInfo(ClientInfoRequestDTO clientInfoRequestDTO) {
        ClientInfo client = new ClientInfo();
        client.setClientName(clientInfoRequestDTO.getClientName());
        client.setContactEmail(clientInfoRequestDTO.getContactEmail());
        client.setPhone(clientInfoRequestDTO.getPhone());
        client.setBusinessName(clientInfoRequestDTO.getBusinessName());
        client.setAddress(clientInfoRequestDTO.getAddress());
        client.setCreatedAt(Instant.now());

        return clientInfoRepository.save(client);
    }

    /**
     * This method is used to save manual payment.
     * @author Mahadi Hasan Joy
     * @since 2025-04-29
     * @param paymentRequestDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment saveManualPayment(PaymentRequestDTO paymentRequestDTO) {
        try {
            // Get invoice to extract all required data
            Invoice invoice = invoiceRepository.findById(paymentRequestDTO.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + paymentRequestDTO.getInvoiceId()));
            
            // Validate invoice has required fields
            if (invoice.getClientAdminId() == null || invoice.getClientAdminId().isBlank()) {
                throw new BillingServiceException("Invoice is missing clientAdminId", HttpStatus.BAD_REQUEST);
            }
            if (invoice.getProductSelections() == null || invoice.getProductSelections().isEmpty()) {
                throw new BillingServiceException("Invoice is missing productSelections", HttpStatus.BAD_REQUEST);
            }
            
            // Check if invoice is already fully paid
            if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
                double totalPaid = calculateTotalPaidAmount(invoice.getId());
                throw new BillingServiceException(
                    String.format("Payment cannot be processed. Invoice is already fully paid. Total paid: %.2f, Invoice total: %.2f",
                        totalPaid, invoice.getTotalAmount()),
                    HttpStatus.BAD_REQUEST
                );
            }
            if (InvoiceStatus.CANCELLED.equals(invoice.getStatus())) {
                throw new BillingServiceException("Invoice is cancelled", HttpStatus.BAD_REQUEST);
            }
            if (InvoiceStatus.EXPIRED.equals(invoice.getStatus())) {
                throw new BillingServiceException("Invoice is expired", HttpStatus.BAD_REQUEST);
            }
            
            // Also check by total paid amount (handles race conditions where status might not be updated yet)
            if (isInvoiceFullyPaid(invoice.getId())) {
                double totalPaid = calculateTotalPaidAmount(invoice.getId());
                throw new BillingServiceException(
                    String.format("Payment cannot be processed. Invoice is already fully paid. Total paid: %.2f, Invoice total: %.2f",
                        totalPaid, invoice.getTotalAmount()),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Derive clientId from invoice
            String clientId = invoice.getClientAdminId();

            // Determine currency: use DTO currency if provided, otherwise default to USD
            String currency = paymentRequestDTO.getCurrency();
            if (currency == null || currency.isBlank()) {
                currency = "USD"; // System default
                log.debug("Currency not provided in request, using default: USD");
            }
            
            // Validate payment amount does not exceed invoice total amount
            double paymentAmount = MoneyUtil.round(paymentRequestDTO.getAmount());
            double invoiceTotalAmount = MoneyUtil.round(invoice.getTotalAmount());
            if (paymentAmount > invoiceTotalAmount + 0.01) {
                throw new BillingServiceException(
                    String.format("Payment amount (%.2f) exceeds invoice total amount (%.2f)", 
                        paymentAmount, invoiceTotalAmount),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Validate payment details if BANK_TRANSFER or CHECK_PAYMENT is selected
            boolean isBankTransferOrCheck = paymentRequestDTO.getPaymentSources().stream()
                    .anyMatch(source -> "BANK_TRANSFER".equalsIgnoreCase(source.getMethod()) || 
                                       "CHECK_PAYMENT".equalsIgnoreCase(source.getMethod()));
            
            if (isBankTransferOrCheck) {
                validatePaymentDetailsMatch(invoice, paymentRequestDTO);
            }
            
            Payment payment = new Payment();
            payment.setActualAmount(paymentAmount);
            payment.setClientId(clientId);
            payment.setInvoiceId(paymentRequestDTO.getInvoiceId());
            // Set mspAdminId, countryId, and roleType from Invoice for filtering
            payment.setMspAdminId(invoice.getMspAdminId());
            payment.setCountryId(invoice.getCountryId());
            payment.setRoleType(invoice.getRoleType());
            payment.setPaymentDate(paymentRequestDTO.getDate());
            payment.setNotes(paymentRequestDTO.getNotes());
            payment.setCurrency(currency);
            payment.setStatus("PENDING");
            payment.setOnline(false); // manual payment, offline
            payment.setActive(false); // inactive until admin verifies
            payment.setCreatedAt(Instant.now());

            // Validate total amount matches payment sources
            double totalSourcesAmount = MoneyUtil.round(
                    paymentRequestDTO.getPaymentSources().stream().mapToDouble(PaymentSourceDTO::getAmount).sum());

            if (!MoneyUtil.isEqual(paymentAmount, totalSourcesAmount)) {
                throw new BillingServiceException("Total amount mismatch with payment sources", HttpStatus.BAD_REQUEST);
            }

            // Set payment sources (credit, bank transfer, etc.)
            payment.setPaymentSources(paymentRequestDTO.getPaymentSources());

            populatePaymentFinancialsFromInvoice(payment, invoice);
            payment.setAmount(paymentAmount);

            // Save to DB
            Payment savedPayment = paymentRepository.save(payment);

            // After saving payment, check if it's Bank Transfer or Check Payment
            // (isBankTransferOrCheck was already checked earlier for validation)
            if (isBankTransferOrCheck) {
                // Mark as SUCCESS immediately (no admin verification needed)
                savedPayment.setStatus("SUCCESS");
                savedPayment.setActive(true);
                savedPayment = paymentRepository.save(savedPayment);
                
                // Update invoice status based on total paid amount (PARTIAL or PAID)
                boolean isFullyPaid = updateInvoiceStatusBasedOnPayments(savedPayment.getInvoiceId(), savedPayment);
                
                // Send payment success notification to billing email
                // Note: invoice variable is already available from the beginning of the method
                try {
                    // Fetch fresh invoice to get updated status
                    Invoice updatedInvoice = invoiceRepository.findById(savedPayment.getInvoiceId()).orElse(null);
                    if (updatedInvoice != null) {
                        paymentNotificationUtil.sendPaymentSuccessNotification(savedPayment, updatedInvoice);
                    } else {
                        log.warn("Invoice not found for payment success notification. InvoiceId: {}", savedPayment.getInvoiceId());
                    }
                } catch (Exception e) {
                    log.error("Failed to send payment success notification: {}", e.getMessage(), e);
                }
                
                // Only activate client admin or MSP if invoice is fully paid
                activateEntityAfterPayment(savedPayment, isFullyPaid, null, savedPayment.getId());
            }

            return savedPayment;
        } catch (BillingServiceException | ResourceNotFoundException | CouponValidationException ex) {
            // Re-throw business exceptions directly with their original status codes
            // Note: CouponNotFoundException, CouponAlreadyExistsException, UnsupportedCouponTypeException,
            // InsufficientCreditBalanceException, and CreditAccountInactiveException all extend BillingServiceException
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while saving manual payment", ex);
            throw new BillingServiceException("Internal error while saving manual payment", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method is used to save online payment.
     * @author Mahadi Hasan Joy
     * @since 2025-04-29
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseDTO saveOnlinePayment(PaymentRequestDTO dto) {
        try {
            // Step 1: Validate payment sources
            double totalSourcesAmount = MoneyUtil.round(
                    dto.getPaymentSources().stream().mapToDouble(PaymentSourceDTO::getAmount).sum());
            double paymentAmount = MoneyUtil.round(dto.getAmount());
            if (!MoneyUtil.isEqual(paymentAmount, totalSourcesAmount)) {
                throw new BillingServiceException("Total amount mismatch with payment sources", HttpStatus.BAD_REQUEST);
            }

            // Get invoice to extract all required data
            Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + dto.getInvoiceId()));

            // Validate invoice has required fields
            if (invoice.getClientAdminId() == null || invoice.getClientAdminId().isBlank()) {
                throw new BillingServiceException("Invoice is missing clientAdminId", HttpStatus.BAD_REQUEST);
            }
            if (invoice.getProductSelections() == null || invoice.getProductSelections().isEmpty()) {
                throw new BillingServiceException("Invoice is missing productSelections", HttpStatus.BAD_REQUEST);
            }

            // Validate invoice state - allow payments for PENDING, ON_PROGRESS, or PARTIAL status
            if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
                double totalPaid = calculateTotalPaidAmount(invoice.getId());
                throw new BillingServiceException(
                    String.format("Payment cannot be processed. Invoice is already fully paid. Total paid: %.2f, Invoice total: %.2f",
                        totalPaid, invoice.getTotalAmount()),
                    HttpStatus.BAD_REQUEST
                );
            }
            if (InvoiceStatus.CANCELLED.equals(invoice.getStatus())) {
                throw new BillingServiceException("Invoice is cancelled", HttpStatus.BAD_REQUEST);
            }
            if (InvoiceStatus.EXPIRED.equals(invoice.getStatus())) {
                throw new BillingServiceException("Invoice is expired", HttpStatus.BAD_REQUEST);
            }
            
            // Also check by total paid amount (handles race conditions where status might not be updated yet)
            if (isInvoiceFullyPaid(invoice.getId())) {
                double totalPaid = calculateTotalPaidAmount(invoice.getId());
                throw new BillingServiceException(
                    String.format("Payment cannot be processed. Invoice is already fully paid. Total paid: %.2f, Invoice total: %.2f",
                        totalPaid, invoice.getTotalAmount()),
                    HttpStatus.BAD_REQUEST
                );
            }
            // Allow: PENDING, ON_PROGRESS, PARTIAL

            // Derive clientId from invoice
            String clientId = invoice.getClientAdminId();

            // Determine currency: use DTO currency if provided, otherwise default to USD
            String currency = dto.getCurrency();
            if (currency == null || currency.isBlank()) {
                currency = "USD"; // System default
                log.debug("Currency not provided in request, using default: USD");
            }

            // Validate payment amount does not exceed invoice total amount
            double invoiceTotalAmount = MoneyUtil.round(invoice.getTotalAmount());
            if (paymentAmount > invoiceTotalAmount + 0.01) {
                throw new BillingServiceException(
                    String.format("Payment amount (%.2f) exceeds invoice total amount (%.2f)", 
                        paymentAmount, invoiceTotalAmount),
                    HttpStatus.BAD_REQUEST
                );
            }

            // Step 2: Create and save the initial Payment object
            Payment payment = new Payment();
            String paymentId = UUID.randomUUID().toString();
            payment.setId(paymentId);
            payment.setClientId(clientId);
            payment.setInvoiceId(dto.getInvoiceId());
            // Set mspAdminId and countryId from Invoice for filtering
            payment.setMspAdminId(invoice.getMspAdminId());
            payment.setCountryId(invoice.getCountryId());
            payment.setRoleType(invoice.getRoleType());
            payment.setPaymentDate(dto.getDate());
            payment.setNotes(dto.getNotes());
            payment.setCurrency(currency);
            payment.setStatus("PENDING");
            payment.setOnline(true);
            payment.setActive(true);
            payment.setCreatedAt(Instant.now());
            populatePaymentFinancialsFromInvoice(payment, invoice);
            payment.setAmount(paymentAmount);

            // Save payment to get paymentId for metadata
            payment = paymentRepository.save(payment);

            // Step 5.1: Update invoice status to ON_PROGRESS
            invoice.setStatus(InvoiceStatus.ON_PROGRESS);
            invoice.setPaymentId(payment.getId());
            invoiceRepository.save(invoice);


            // Step 6: Handle payment sources
            double gatewayAmount = 0.0;
            String gatewayMethod = null;

            for (PaymentSourceDTO source : dto.getPaymentSources()) {
                if ("CREDIT".equalsIgnoreCase(source.getMethod())) {
                    Credit credit = creditRepository.findByClientId(clientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Credit account not found for clientId: " + clientId));
                    if (!credit.isActive()) {
                        throw new CreditAccountInactiveException("Credit account is inactive.");
                    }
                    if (credit.getAvailableAmount() < source.getAmount()) {
                        throw new InsufficientCreditBalanceException("Insufficient credits for this payment.");
                    }

                    credit.setAvailableAmount(credit.getAvailableAmount() - source.getAmount());
                    credit.setUpdatedAt(Instant.now());
                    creditRepository.save(credit);

                    CreditTransactionCreateDTO tx = new CreditTransactionCreateDTO();
                    tx.setClientId(clientId);
                    tx.setType("USAGE");
                    tx.setStatus(TransactionStatus.UNPAID);
                    tx.setAmount(source.getAmount());
                    tx.setReferenceType("PAYMENT");
                    tx.setReferenceId(dto.getInvoiceId());
                    tx.setDescription("Partial payment using credits for Invoice " + dto.getInvoiceId());
                    tx.setInitiatedBy(clientId);

                    creditService.logCreditTransaction(tx);
                } else if ("STRIPE".equalsIgnoreCase(source.getMethod()) || "PAYPAL".equalsIgnoreCase(source.getMethod())) {
                    gatewayAmount += source.getAmount();
                    gatewayMethod = source.getMethod();
                } else {
                    throw new BillingServiceException("Unsupported payment method: " + source.getMethod(), HttpStatus.BAD_REQUEST);
                }
            }

            GatewayResponseDTO gatewayResponse = null;

            // Step 7: Process online gateway payment if applicable
            if (gatewayAmount > 0.0) {
                PaymentGatewayRequestDTO gatewayRequest = new PaymentGatewayRequestDTO();
                gatewayRequest.setPaymentId(payment.getId());
                gatewayRequest.setInvoiceId(payment.getInvoiceId());
                gatewayRequest.setClientId(payment.getClientId());
                gatewayRequest.setCurrency(payment.getCurrency());
                gatewayRequest.setAmount(gatewayAmount);

                switch (gatewayMethod.toLowerCase()) {
                    case "stripe":
                        gatewayResponse = processStripePayment(gatewayRequest);
                        break;
                    case "paypal":
                        gatewayResponse = processPaypalPayment(gatewayRequest);
                        break;
                    default:
                        throw new BillingServiceException("Unsupported online payment method", HttpStatus.BAD_REQUEST);
                }

                payment.setTransactionId(gatewayResponse.getTransactionId());

                List<PaymentSourceDTO> updatedSources = new ArrayList<>();
                for (PaymentSourceDTO source : dto.getPaymentSources()) {
                    if (source.getMethod().equalsIgnoreCase(gatewayMethod)) {
                        source.setTransactionId(gatewayResponse.getTransactionId());
                    }
                    updatedSources.add(source);
                }
                payment.setPaymentSources(updatedSources);

            } else {
                // Fully paid via credits
                payment.setStatus("SUCCESS");
                payment.setPaymentSources(dto.getPaymentSources());
            }

            // Step 8: Final save with updated info
            payment = paymentRepository.save(payment);

            // Step 9: Commission logic
            commissionRateService.applyCommissionIfEligible(payment.getClientId(), payment.getSubtotal(), payment.getInvoiceId());

            // Step 10: Return result
            if (gatewayResponse != null) {
                return new PaymentResponseDTO(payment.getId(), gatewayResponse.getCheckoutUrl(), "Payment session created successfully");
            } else {
                return new PaymentResponseDTO(payment.getId(), null, "Payment completed fully with credits");
            }

        } catch (BillingServiceException | ResourceNotFoundException | CouponValidationException ex) {
            // Re-throw business exceptions directly with their original status codes
            // Note: CouponNotFoundException, CouponAlreadyExistsException, UnsupportedCouponTypeException,
            // InsufficientCreditBalanceException, and CreditAccountInactiveException all extend BillingServiceException
            throw ex;
        } catch (Exception ex) {
            log.error("Error while processing online payment", ex);
            throw new BillingServiceException("Failed to complete online payment", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public WebhookResponseDTO processStripeWebhook(String payload, String stripeSignature) {
        Event event;
        
        // Check if this is a test request (for local development/testing)
        // Test mode: if signature starts with "test_" or is the webhook secret itself, bypass validation
        boolean isTestMode = stripeSignature != null && 
                            (stripeSignature.startsWith("test_") || 
                             stripeSignature.equals(stripeWebhookSecret) ||
                             stripeSignature.equals("TEST_MODE_BYPASS"));
        
        if (stripeSignature == null && !isTestMode) {
            log.warn("Received Stripe webhook without signature header");
            return new WebhookResponseDTO("error", "Missing Stripe signature", null, null);
        }

        try {
            if (isTestMode) {
                log.warn("Test mode: Bypassing Stripe signature validation");
                // Parse event directly without signature validation for testing
                com.google.gson.JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                event = com.stripe.net.ApiResource.GSON.fromJson(jsonObject, Event.class);
            } else {
                // Normal production flow with signature validation
                event = Webhook.constructEvent(payload, stripeSignature, stripeWebhookSecret);
            }
            
            String eventType = event.getType();
            String eventId = event.getId();
            log.info("Processing Stripe event. Type: {}, ID: {}, LiveMode: {}", 
                eventType, eventId, event.getLivemode());

            switch (eventType) {
                case "checkout.session.completed":
                    log.info("Processing checkout.session.completed event");
                    Session session;
                    if (isTestMode) {
                        // In test mode, manually deserialize Session from JSON
                        try {
                            com.google.gson.JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                            com.google.gson.JsonObject dataObject = jsonObject.getAsJsonObject("data");
                            com.google.gson.JsonObject sessionObject = dataObject.getAsJsonObject("object");
                            session = com.stripe.net.ApiResource.GSON.fromJson(sessionObject, Session.class);
                            log.info("Test mode: Session deserialized. SessionId: {}, PaymentStatus: {}", 
                                session.getId(), session.getPaymentStatus());
                        } catch (Exception e) {
                            log.error("Failed to deserialize Session in test mode: {}", e.getMessage(), e);
                            return new WebhookResponseDTO("error", "Failed to parse session object in test mode: " + e.getMessage(), null, null);
                        }
                    } else {
                        // Normal production flow
                        session = (Session) event.getDataObjectDeserializer()
                                .getObject()
                                .orElseThrow(() -> new RuntimeException("Stripe session object missing"));
                    }

                    String sessionId = session.getId();
                    String paymentStatus = session.getPaymentStatus();
                    String paymentType = null;
                    String paymentId = null;
                    
                    if (session.getMetadata() != null) {
                        paymentType = session.getMetadata().get("paymentType");
                        paymentId = session.getMetadata().get("paymentId");
                        log.info("Session details - SessionId: {}, PaymentStatus: {}, PaymentType: {}, PaymentId: {}, InvoiceId: {}", 
                            sessionId, paymentStatus, paymentType, paymentId, session.getMetadata().get("invoiceId"));
                    } else {
                        log.warn("Session metadata is null");
                    }

                    if ("CREDIT_PAYMENT".equalsIgnoreCase(paymentType)) {
                        log.info("Credit payment webhook received. SessionId: {}, PaymentId: {}", sessionId, paymentId);
                        return new WebhookResponseDTO("success", "Credit payment logged", sessionId, paymentId);
                    }

                    // Keep original logic for PRODUCT_PAYMENT and others
                    // For test mode, we need to manually call updatePaymentStatus
                    if (isTestMode) {
                        String transactionId = session.getId();
                        log.info("Test mode: Updating payment status. PaymentId: {}, Status: {}, TransactionId: {}", 
                            paymentId, paymentStatus, transactionId);
                        updatePaymentStatus(paymentId, paymentStatus, transactionId);
                    } else {
                        handleCheckoutSessionCompleted(event);
                    }
                    break;

                case "invoice.paid":
                    log.info("Processing invoice.paid event");
                    handleInvoicePaid(event);
                    break;

                case "payment_intent.created":
                    log.info("Processing payment_intent.created event");
                    WebhookResponseDTO createdResponse = handlePaymentIntentCreated(event);
                    if (createdResponse != null) {
                        return createdResponse;
                    }
                    break;

                case "payment_intent.succeeded":
                    log.info("Processing payment_intent.succeeded event");
                    handlePaymentIntentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    log.info("Processing payment_intent.payment_failed event");
                    handlePaymentIntentFailed(event);
                    break;

                default:
                    log.info("Ignored Stripe event type: {}", eventType);
                    return new WebhookResponseDTO("ignored", "Event type not handled", null, null);
            }

            log.info("Successfully processed Stripe event: {}", eventType);
            return new WebhookResponseDTO("success", "Processed event: " + eventType, null, null);

        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature: {}", e.getMessage());
            return new WebhookResponseDTO("error", "Invalid Stripe signature", null, null);
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            return new WebhookResponseDTO("error", "Webhook processing failed", null, null);
        }
    }


    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) {
            log.warn("Session object is null in checkout.session.completed event");
            return;
        }

        String transactionId = session.getId();
        String paymentStatus = session.getPaymentStatus(); // 'paid', etc.
        String paymentId = session.getMetadata() != null ? session.getMetadata().get("paymentId") : null;

        log.info("Handling checkout session completed. PaymentId: {}, Status: {}, TransactionId: {}", 
            paymentId, paymentStatus, transactionId);
        updatePaymentStatus(paymentId, paymentStatus, transactionId);
    }
    private void handleInvoicePaid(Event event) {
        com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice)
                event.getDataObjectDeserializer().getObject().orElse(null);

        if (stripeInvoice == null) {
            log.warn("Stripe invoice object is null in invoice.paid event");
            return;
        }

        String stripeInvoiceId = stripeInvoice.getId();
        String paymentId = stripeInvoice.getMetadata() != null ? stripeInvoice.getMetadata().get("paymentId") : null;

        log.info("Processing invoice.paid event. StripeInvoiceId: {}, PaymentId: {}", stripeInvoiceId, paymentId);

        if (paymentId != null) {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                payment.setStatus("SUCCESS");
                payment.setTransactionId(stripeInvoice.getCharge());
                paymentRepository.save(payment);
                log.info("Payment updated to SUCCESS. PaymentId: {}, InvoiceId: {}", paymentId, payment.getInvoiceId());

                // Update invoice status based on total paid amount (PARTIAL or PAID)
                boolean isFullyPaid = updateInvoiceStatusBasedOnPayments(payment.getInvoiceId(), payment);
                log.info("Invoice status updated. InvoiceId: {}, FullyPaid: {}", payment.getInvoiceId(), isFullyPaid);
                
                // Send payment success notification to billing email
                try {
                    Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
                    if (invoice != null) {
                        paymentNotificationUtil.sendPaymentSuccessNotification(payment, invoice);
                    } else {
                        log.warn("Invoice not found for payment success notification. InvoiceId: {}", payment.getInvoiceId());
                    }
                } catch (Exception e) {
                    log.error("Failed to send payment success notification: {}", e.getMessage(), e);
                }
                
                // Only activate client admin or MSP if invoice is fully paid
                activateEntityAfterPayment(payment, isFullyPaid, null, paymentId);
            } else {
                log.warn("Payment not found for paymentId: {}", paymentId);
            }
        } else {
            log.warn("invoice.paid event received without paymentId metadata. StripeInvoiceId: {}", stripeInvoiceId);
        }
    }

    private WebhookResponseDTO handlePaymentIntentCreated(Event event) {
        log.info("Processing payment_intent.created event. EventId: {}", event.getId());

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        // Try Stripe SDK deserialization first
        if (deserializer.getObject().isPresent() && deserializer.getObject().get() instanceof PaymentIntent) {
            PaymentIntent intent = (PaymentIntent) deserializer.getObject().get();
            String paymentIntentId = intent.getId();
            String paymentIntentStatus = intent.getStatus();
            Map<String, String> metadata = intent.getMetadata();
            String paymentId = metadata != null ? metadata.get("paymentId") : null;

            log.info("PaymentIntent created. PaymentIntentId: {}, Status: {}, PaymentId: {}", 
                paymentIntentId, paymentIntentStatus, paymentId);

            if (paymentId == null) {
                log.warn("No paymentId found in PaymentIntent metadata. PaymentIntentId: {}", paymentIntentId);
                return new WebhookResponseDTO("error", "PaymentId not found in metadata", paymentIntentId, null);
            }

            // Update payment record with payment intent ID for tracking
            // Keep status as PENDING since payment hasn't been completed yet
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                // Store payment intent ID in transactionId for tracking
                if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                    payment.setTransactionId(paymentIntentId);
                    paymentRepository.save(payment);
                    log.info("Payment record updated with PaymentIntent ID. PaymentId: {}, PaymentIntentId: {}, Status: {}", 
                        paymentId, paymentIntentId, payment.getStatus());
                } else {
                    log.info("Payment already has transactionId. PaymentId: {}, ExistingTransactionId: {}, PaymentIntentId: {}", 
                        paymentId, payment.getTransactionId(), paymentIntentId);
                }
                return new WebhookResponseDTO("success", "Payment intent created and linked to payment", paymentIntentId, paymentId);
            } else {
                log.warn("Payment not found for paymentId: {}", paymentId);
                return new WebhookResponseDTO("error", "Payment not found", paymentIntentId, paymentId);
            }
        }

        // Fallback to raw JSON parsing if SDK failed
        log.warn("Stripe SDK deserialization failed, using fallback JSON parsing");

        try {
            String rawJson = deserializer.getRawJson();
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();

            String paymentIntentId = root.get("id").getAsString();
            String paymentIntentStatus = root.has("status") ? root.get("status").getAsString() : "unknown";
            JsonObject metadata = root.has("metadata") ? root.getAsJsonObject("metadata") : null;
            String paymentId = (metadata != null && metadata.has("paymentId"))
                    ? metadata.get("paymentId").getAsString()
                    : null;

            log.info("PaymentIntent created (fallback). PaymentIntentId: {}, Status: {}, PaymentId: {}", 
                paymentIntentId, paymentIntentStatus, paymentId);

            if (paymentId == null) {
                log.warn("No paymentId found in fallback metadata. PaymentIntentId: {}", paymentIntentId);
                return new WebhookResponseDTO("error", "PaymentId not found in metadata", paymentIntentId, null);
            }

            // Update payment record with payment intent ID
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                    payment.setTransactionId(paymentIntentId);
                    paymentRepository.save(payment);
                    log.info("Payment record updated with PaymentIntent ID (fallback). PaymentId: {}, PaymentIntentId: {}", 
                        paymentId, paymentIntentId);
                }
                return new WebhookResponseDTO("success", "Payment intent created and linked to payment", paymentIntentId, paymentId);
            } else {
                log.warn("Payment not found for paymentId: {}", paymentId);
                return new WebhookResponseDTO("error", "Payment not found", paymentIntentId, paymentId);
            }
        } catch (Exception e) {
            log.error("Failed to handle payment_intent.created: {}", e.getMessage(), e);
            return new WebhookResponseDTO("error", "Failed to process payment_intent.created: " + e.getMessage(), null, null);
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        log.info("Processing payment_intent.succeeded event. EventId: {}", event.getId());

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        // Try Stripe SDK deserialization first
        if (deserializer.getObject().isPresent() && deserializer.getObject().get() instanceof PaymentIntent) {
            PaymentIntent intent = (PaymentIntent) deserializer.getObject().get();
            Map<String, String> metadata = intent.getMetadata();
            String paymentId = metadata != null ? metadata.get("paymentId") : null;

            if (paymentId == null) {
                log.warn("No paymentId found in PaymentIntent metadata. PaymentIntentId: {}", intent.getId());
                return;
            }

            log.info("Updating payment status from payment_intent.succeeded. PaymentId: {}, PaymentIntentId: {}", 
                paymentId, intent.getId());
            updatePaymentStatus(paymentId, "paid", intent.getId());
            return;
        }

        // Fallback to raw JSON parsing if SDK failed
        log.warn("Stripe SDK deserialization failed, using fallback JSON parsing");

        try {
            String rawJson = deserializer.getRawJson();
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();

            String transactionId = root.get("id").getAsString();
            JsonObject metadata = root.has("metadata") ? root.getAsJsonObject("metadata") : null;
            String paymentId = (metadata != null && metadata.has("paymentId"))
                    ? metadata.get("paymentId").getAsString()
                    : null;

            if (paymentId == null) {
                log.warn("No paymentId found in fallback metadata. TransactionId: {}", transactionId);
                return;
            }

            log.info("Updating payment status from payment_intent.succeeded (fallback). PaymentId: {}, TransactionId: {}", 
                paymentId, transactionId);
            updatePaymentStatus(paymentId, "paid", transactionId);
        } catch (Exception e) {
            log.error("Failed to handle payment_intent.succeeded: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (intent == null) {
            log.warn("PaymentIntent object is null in payment_intent.payment_failed event");
            return;
        }

        String paymentId = intent.getMetadata() != null ? intent.getMetadata().get("paymentId") : null;
        log.warn("PaymentIntent failed. PaymentIntentId: {}, PaymentId: {}", intent.getId(), paymentId);
        
        if (paymentId != null) {
            // Update payment status to FAILED
            updatePaymentStatus(paymentId, "failed", intent.getId());
            
            // Send payment failure notification to billing email
            try {
                Payment payment = paymentRepository.findById(paymentId).orElse(null);
                if (payment != null) {
                    Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
                    if (invoice != null) {
                        paymentNotificationUtil.sendPaymentFailureNotification(payment, invoice);
                    } else {
                        log.warn("Invoice not found for payment failure notification. InvoiceId: {}", payment.getInvoiceId());
                    }
                } else {
                    log.warn("Payment not found for payment failure notification. PaymentId: {}", paymentId);
                }
            } catch (Exception e) {
                log.error("Failed to send payment failure notification: {}", e.getMessage(), e);
            }
        } else {
            log.warn("PaymentIntent failed but paymentId is null in metadata");
        }
    }

    public void updatePaymentStatus(String paymentId, String paymentStatus, String transactionId) {
        if (paymentId == null) {
            log.warn("Cannot update payment status: paymentId is null");
            return;
        }
        
        log.info("Updating payment status. PaymentId: {}, Status: {}, TransactionId: {}", 
            paymentId, paymentStatus, transactionId);

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            log.warn("Payment not found. PaymentId: {}", paymentId);
            return;
        }

        if ("paid".equalsIgnoreCase(paymentStatus)) {
            Invoice invoiceForGuard = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
            if (invoiceForGuard != null && (InvoiceStatus.CANCELLED.equals(invoiceForGuard.getStatus())
                    || InvoiceStatus.EXPIRED.equals(invoiceForGuard.getStatus()))) {
                log.warn("Ignoring payment success for {} invoice. PaymentId: {}, InvoiceId: {}",
                        invoiceForGuard.getStatus(), paymentId, payment.getInvoiceId());
                payment.setStatus("FAILED");
                payment.setTransactionId(transactionId);
                paymentRepository.save(payment);
                refundCreditsIfNeeded(payment);
                return;
            }

            payment.setStatus("SUCCESS");
            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);
            
            // Update invoice status based on total paid amount (PARTIAL or PAID)
            boolean isFullyPaid = updateInvoiceStatusBasedOnPayments(payment.getInvoiceId(), payment);
            log.info("Invoice status updated. InvoiceId: {}, FullyPaid: {}", payment.getInvoiceId(), isFullyPaid);

            // Send payment success notification to billing email
            try {
                Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
                if (invoice != null) {
                    paymentNotificationUtil.sendPaymentSuccessNotification(payment, invoice);
                } else {
                    log.warn("Invoice not found for payment success notification. InvoiceId: {}", payment.getInvoiceId());
                }
            } catch (Exception e) {
                log.error("Failed to send payment success notification: {}", e.getMessage(), e);
            }
            
            // Only activate client admin or MSP if invoice is fully paid
            activateEntityAfterPayment(payment, isFullyPaid, null, paymentId);
            
            log.info("Payment updated to SUCCESS. PaymentId: {}, InvoiceId: {}, ClientId: {}", 
                paymentId, payment.getInvoiceId(), payment.getClientId());
        } else {
            payment.setStatus("FAILED");
            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);
            refundCreditsIfNeeded(payment);
            
            // Send payment failure notification to billing email
            try {
                Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
                if (invoice != null) {
                    paymentNotificationUtil.sendPaymentFailureNotification(payment, invoice);
                } else {
                    log.warn("Invoice not found for payment failure notification. InvoiceId: {}", payment.getInvoiceId());
                }
            } catch (Exception e) {
                log.error("Failed to send payment failure notification: {}", e.getMessage(), e);
            }
            
            log.warn("Payment updated to FAILED. PaymentId: {}, InvoiceId: {}", paymentId, payment.getInvoiceId());
        }
    }

    /**
     * Calculates the total amount paid for an invoice by summing all successful payments.
     * 
     * @param invoiceId The invoice ID
     * @return Total paid amount (0.0 if no successful payments)
     */
    private double calculateTotalPaidAmount(String invoiceId) {
        List<Payment> successfulPayments = paymentRepository.findByInvoiceId(invoiceId).stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getAmount() != null)
                .toList();
        
        return MoneyUtil.round(successfulPayments.stream()
                .mapToDouble(p -> p.getAmount())
                .sum());
    }

    /**
     * Checks if an invoice is already fully paid by comparing total paid amount with invoice total.
     * 
     * @param invoiceId The invoice ID
     * @return true if invoice is fully paid (totalPaid >= invoiceTotal within tolerance), false otherwise
     */
    private boolean isInvoiceFullyPaid(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
        double totalPaid = calculateTotalPaidAmount(invoiceId);
        return totalPaid >= MoneyUtil.round(invoice.getTotalAmount()) - 0.01;
    }

    /**
     * Updates invoice status based on total paid amount across all payments.
     * Sets status to PARTIAL when partially paid, PAID when fully paid.
     * 
     * @param invoiceId The invoice ID to update
     * @param currentPayment The current payment being processed (optional, for payment details)
     * @return true if invoice is fully paid, false if partially paid
     */
    private boolean updateInvoiceStatusBasedOnPayments(String invoiceId, Payment currentPayment) {
        // Fetch fresh invoice to avoid stale object issues
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (InvoiceStatus.CANCELLED.equals(invoice.getStatus())
                || InvoiceStatus.EXPIRED.equals(invoice.getStatus())) {
            throw new BillingServiceException(
                    "Cannot update payment status for a " + invoice.getStatus().name().toLowerCase()
                            + " invoice: " + invoiceId,
                    HttpStatus.BAD_REQUEST
            );
        }

        // Don't update if already PAID (unless we're recalculating)
        if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
            // Recalculate to verify it's actually fully paid
            double totalPaid = calculateTotalPaidAmount(invoiceId);
            if (totalPaid >= MoneyUtil.round(invoice.getTotalAmount()) - 0.01) {
                log.debug("Invoice {} already marked as PAID and is fully paid. TotalPaid: {}, TotalAmount: {}", 
                    invoiceId, totalPaid, invoice.getTotalAmount());
                return true;
            } else {
                // Status is PAID but not actually fully paid - this shouldn't happen, but handle it
                log.warn("Invoice {} marked as PAID but totalPaid ({}) < totalAmount ({}). Recalculating status.", 
                    invoiceId, totalPaid, invoice.getTotalAmount());
            }
        }

        // Calculate total paid amount
        double totalPaid = calculateTotalPaidAmount(invoiceId);
        double invoiceTotal = MoneyUtil.round(invoice.getTotalAmount());

        // Update payment details if provided (for bank transfer/check)
        if (currentPayment != null && currentPayment.getPaymentSources() != null && !currentPayment.getPaymentSources().isEmpty()) {
            // Find bank transfer source
            PaymentSourceDTO bankTransferSource = currentPayment.getPaymentSources().stream()
                    .filter(source -> "BANK_TRANSFER".equalsIgnoreCase(source.getMethod()))
                    .findFirst()
                    .orElse(null);

            // Find check payment source
            PaymentSourceDTO checkSource = currentPayment.getPaymentSources().stream()
                    .filter(source -> "CHECK_PAYMENT".equalsIgnoreCase(source.getMethod()))
                    .findFirst()
                    .orElse(null);

            // Update invoice with payment method and details from metadata
            if (bankTransferSource != null && bankTransferSource.getMetadata() != null) {
                invoice.setPaymentMethod(PaymentMethodType.BANK_TRANSFER);
                invoice.setPaymentDetails(bankTransferSource.getMetadata());
            } else if (checkSource != null && checkSource.getMetadata() != null) {
                invoice.setPaymentMethod(PaymentMethodType.CHECK_PAYMENT);
                invoice.setPaymentDetails(checkSource.getMetadata());
            }
        }

        // Link payment to invoice (use current payment if provided, otherwise keep existing)
        if (currentPayment != null) {
            invoice.setPaymentId(currentPayment.getId());
        }

        // Determine status based on total paid amount
        boolean isFullyPaid = false;
        if (totalPaid >= invoiceTotal - 0.01) {
            // Fully paid (within tolerance)
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
            isFullyPaid = true;
            log.info("Invoice {} marked as PAID. TotalPaid: {}, TotalAmount: {}", invoiceId, totalPaid, invoiceTotal);
        } else if (totalPaid > 0) {
            // Partially paid
            invoice.setStatus(InvoiceStatus.PARTIAL);
            invoice.setPaidAt(null); // Not fully paid yet
            isFullyPaid = false;
            log.info("Invoice {} marked as PARTIAL. TotalPaid: {}, TotalAmount: {}, Outstanding: {}", 
                invoiceId, totalPaid, invoiceTotal, invoiceTotal - totalPaid);
        } else {
            // No payments yet - keep current status (PENDING or ON_PROGRESS)
            log.debug("Invoice {} has no successful payments yet. TotalPaid: {}, CurrentStatus: {}", 
                invoiceId, totalPaid, invoice.getStatus());
        }

        invoiceRepository.save(invoice);
        return isFullyPaid;
    }

    public void updateInvoiceStatusToPaid(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
            throw new BillingServiceException("Invoice is already marked as PAID", HttpStatus.BAD_REQUEST);
        }
        if (InvoiceStatus.CANCELLED.equals(invoice.getStatus())) {
            throw new BillingServiceException("Cannot mark a cancelled invoice as PAID", HttpStatus.BAD_REQUEST);
        }
        if (InvoiceStatus.EXPIRED.equals(invoice.getStatus())) {
            throw new BillingServiceException("Cannot mark an expired invoice as PAID", HttpStatus.BAD_REQUEST);
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());

        invoiceRepository.save(invoice);
    }

    /**
     * Updates invoice status to PAID and payment details in a single atomic operation.
     * This prevents the stale object issue where a separate update could overwrite the status.
     *
     * @param invoiceId The invoice ID to update
     * @param payment The payment containing payment sources with metadata
     */
    private void updateInvoiceToPaidWithPaymentDetails(String invoiceId, Payment payment) {
        // Fetch fresh invoice to avoid stale object issues
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
            throw new BillingServiceException("Invoice is already marked as PAID", HttpStatus.BAD_REQUEST);
        }
        if (InvoiceStatus.CANCELLED.equals(invoice.getStatus())) {
            throw new BillingServiceException("Cannot mark a cancelled invoice as PAID", HttpStatus.BAD_REQUEST);
        }
        if (InvoiceStatus.EXPIRED.equals(invoice.getStatus())) {
            throw new BillingServiceException("Cannot mark an expired invoice as PAID", HttpStatus.BAD_REQUEST);
        }

        // Update status and timestamp
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());

        // Update payment details from payment sources metadata
        if (payment.getPaymentSources() != null && !payment.getPaymentSources().isEmpty()) {
            // Find bank transfer source
            PaymentSourceDTO bankTransferSource = payment.getPaymentSources().stream()
                    .filter(source -> "BANK_TRANSFER".equalsIgnoreCase(source.getMethod()))
                    .findFirst()
                    .orElse(null);

            // Find check payment source
            PaymentSourceDTO checkSource = payment.getPaymentSources().stream()
                    .filter(source -> "CHECK_PAYMENT".equalsIgnoreCase(source.getMethod()))
                    .findFirst()
                    .orElse(null);

            // Update invoice with payment method and details from metadata
            if (bankTransferSource != null && bankTransferSource.getMetadata() != null) {
                invoice.setPaymentMethod(PaymentMethodType.BANK_TRANSFER);
                invoice.setPaymentDetails(bankTransferSource.getMetadata());
            } else if (checkSource != null && checkSource.getMetadata() != null) {
                invoice.setPaymentMethod(PaymentMethodType.CHECK_PAYMENT);
                invoice.setPaymentDetails(checkSource.getMetadata());
            }
        }

        // Link payment to invoice
        invoice.setPaymentId(payment.getId());

        // Single atomic save operation
        invoiceRepository.save(invoice);
    }

    /**
     * Updates invoice payment details from payment sources metadata.
     * This method extracts payment method and details from payment sources and updates the invoice.
     *
     * @param invoice The invoice to update
     * @param payment The payment containing payment sources with metadata
     */
    private void updateInvoicePaymentDetailsFromPayment(Invoice invoice, Payment payment) {
        if (payment.getPaymentSources() == null || payment.getPaymentSources().isEmpty()) {
            return;
        }

        // Find bank transfer source
        PaymentSourceDTO bankTransferSource = payment.getPaymentSources().stream()
                .filter(source -> "BANK_TRANSFER".equalsIgnoreCase(source.getMethod()))
                .findFirst()
                .orElse(null);

        // Find check payment source
        PaymentSourceDTO checkSource = payment.getPaymentSources().stream()
                .filter(source -> "CHECK_PAYMENT".equalsIgnoreCase(source.getMethod()))
                .findFirst()
                .orElse(null);

        // Update invoice with payment method and details from metadata
        if (bankTransferSource != null && bankTransferSource.getMetadata() != null) {
            invoice.setPaymentMethod(PaymentMethodType.BANK_TRANSFER);
            invoice.setPaymentDetails(bankTransferSource.getMetadata());
        } else if (checkSource != null && checkSource.getMetadata() != null) {
            invoice.setPaymentMethod(PaymentMethodType.CHECK_PAYMENT);
            invoice.setPaymentDetails(checkSource.getMetadata());
        }

        // Link payment to invoice
        invoice.setPaymentId(payment.getId());
        invoiceRepository.save(invoice);
    }

    /**
     * Validates that payment details provided in the request match what was stored in the invoice during onboarding.
     * This ensures that the client admin is using the correct payment details that were provided during onboarding.
     *
     * @param invoice The invoice containing stored payment details
     * @param paymentRequestDTO The payment request containing payment details to validate
     */
    private void validatePaymentDetailsMatch(Invoice invoice, PaymentRequestDTO paymentRequestDTO) {
        // Check if invoice has payment details stored
        if (invoice.getPaymentMethod() == null || invoice.getPaymentDetails() == null) {
            throw new BillingServiceException(
                "Invoice does not have payment details stored. Please contact administrator.", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Find BANK_TRANSFER or CHECK_PAYMENT source in request
        PaymentSourceDTO paymentSource = paymentRequestDTO.getPaymentSources().stream()
            .filter(source -> "BANK_TRANSFER".equalsIgnoreCase(source.getMethod()) || 
                             "CHECK_PAYMENT".equalsIgnoreCase(source.getMethod()))
            .findFirst()
            .orElse(null);
        
        if (paymentSource == null || paymentSource.getMetadata() == null) {
            throw new BillingServiceException(
                "Payment details (metadata) are required for BANK_TRANSFER or CHECK_PAYMENT", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate payment method matches
        PaymentMethodType invoiceMethod = invoice.getPaymentMethod();
        String requestMethod = paymentSource.getMethod();
        
        if (!invoiceMethod.name().equalsIgnoreCase(requestMethod)) {
            throw new BillingServiceException(
                String.format("Payment method mismatch. Invoice has %s but request has %s", 
                    invoiceMethod, requestMethod), 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate payment details match based on method type
        if (invoiceMethod == PaymentMethodType.BANK_TRANSFER) {
            validateBankTransferDetails(invoice.getPaymentDetails(), paymentSource.getMetadata());
        } else if (invoiceMethod == PaymentMethodType.CHECK_PAYMENT) {
            validateCheckPaymentDetails(invoice.getPaymentDetails(), paymentSource.getMetadata());
        }
    }

    /**
     * Validates bank transfer details match between invoice and request.
     * Primary validation: transactionNumber must match exactly.
     * Secondary validation: bankName, accountNumber, paymentAmount should match.
     *
     * @param invoicePaymentDetails Payment details stored in invoice (Object)
     * @param requestMetadata Payment details from request (Object)
     */
    private void validateBankTransferDetails(Object invoicePaymentDetails, Object requestMetadata) {
        try {
            // Convert Object to BankTransferDetailsDto
            BankTransferDetailsDto invoiceDetails = objectMapper.convertValue(invoicePaymentDetails, BankTransferDetailsDto.class);
            BankTransferDetailsDto requestDetails = objectMapper.convertValue(requestMetadata, BankTransferDetailsDto.class);
            
            // Primary validation: transactionNumber must match
            if (invoiceDetails.getTransactionNumber() == null || requestDetails.getTransactionNumber() == null ||
                !invoiceDetails.getTransactionNumber().equalsIgnoreCase(requestDetails.getTransactionNumber())) {
                throw new BillingServiceException(
                    "Transaction number does not match the stored payment details", 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Secondary validation: other fields should match
            if (invoiceDetails.getBankName() != null && requestDetails.getBankName() != null &&
                !invoiceDetails.getBankName().equalsIgnoreCase(requestDetails.getBankName())) {
                log.warn("Bank name mismatch: invoice has '{}' but request has '{}'", 
                    invoiceDetails.getBankName(), requestDetails.getBankName());
            }
            
            if (invoiceDetails.getAccountNumber() != null && requestDetails.getAccountNumber() != null &&
                !invoiceDetails.getAccountNumber().equals(requestDetails.getAccountNumber())) {
                log.warn("Account number mismatch: invoice has '{}' but request has '{}'", 
                    invoiceDetails.getAccountNumber(), requestDetails.getAccountNumber());
            }
            
            if (invoiceDetails.getPaymentAmount() != null && requestDetails.getPaymentAmount() != null &&
                Math.abs(invoiceDetails.getPaymentAmount() - requestDetails.getPaymentAmount()) > 0.01) {
                log.warn("Payment amount mismatch: invoice has '{}' but request has '{}'", 
                    invoiceDetails.getPaymentAmount(), requestDetails.getPaymentAmount());
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert payment details to BankTransferDetailsDto", e);
            throw new BillingServiceException(
                "Invalid bank transfer payment details format", 
                HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Validates check payment details match between invoice and request.
     * Primary validation: checkNumber must match exactly.
     * Secondary validation: bankName, paymentDate should match.
     *
     * @param invoicePaymentDetails Payment details stored in invoice (Object)
     * @param requestMetadata Payment details from request (Object)
     */
    private void validateCheckPaymentDetails(Object invoicePaymentDetails, Object requestMetadata) {
        try {
            // Convert Object to CheckPaymentDetailsDto
            CheckPaymentDetailsDto invoiceDetails = objectMapper.convertValue(invoicePaymentDetails, CheckPaymentDetailsDto.class);
            CheckPaymentDetailsDto requestDetails = objectMapper.convertValue(requestMetadata, CheckPaymentDetailsDto.class);
            
            // Primary validation: checkNumber must match
            if (invoiceDetails.getCheckNumber() == null || requestDetails.getCheckNumber() == null ||
                !invoiceDetails.getCheckNumber().equalsIgnoreCase(requestDetails.getCheckNumber())) {
                throw new BillingServiceException(
                    "Check number does not match the stored payment details", 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Secondary validation: other fields should match
            if (invoiceDetails.getBankName() != null && requestDetails.getBankName() != null &&
                !invoiceDetails.getBankName().equalsIgnoreCase(requestDetails.getBankName())) {
                log.warn("Bank name mismatch: invoice has '{}' but request has '{}'", 
                    invoiceDetails.getBankName(), requestDetails.getBankName());
            }
            
            if (invoiceDetails.getPaymentDate() != null && requestDetails.getPaymentDate() != null &&
                !invoiceDetails.getPaymentDate().equals(requestDetails.getPaymentDate())) {
                log.warn("Payment date mismatch: invoice has '{}' but request has '{}'", 
                    invoiceDetails.getPaymentDate(), requestDetails.getPaymentDate());
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert payment details to CheckPaymentDetailsDto", e);
            throw new BillingServiceException(
                "Invalid check payment details format", 
                HttpStatus.BAD_REQUEST
            );
        }
    }

    public void activateClientAdmin(String clientId) {
        try {
            String url = registrationUrl + "/client/admin/activate-license/" + clientId;
            webClient.put()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .block();  // Wait for response (you can make it async if needed)

            log.info("Client admin {} activated successfully via registration service", clientId);
        } catch (WebClientResponseException e) {
            log.error("Failed to activate client admin {}: {} - {}", clientId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BillingServiceException("Failed to activate client admin", HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Unexpected error while activating client admin {}", clientId, e);
            throw new BillingServiceException("Unexpected error calling registration service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void activateMsp(String mspId) {
        activateMsp(mspId, null);
    }

    public void activateMsp(String mspId, String invoiceId) {
        try {
            String url = registrationUrl + "/msp/activate-license/" + mspId;
            if (invoiceId != null && !invoiceId.isBlank()) {
                url += "?invoiceId=" + invoiceId;
            }
            webClient.put()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("MSP {} activated successfully via registration service (invoiceId={})", mspId, invoiceId);
        } catch (WebClientResponseException e) {
            log.error("Failed to activate MSP {}: {} - {}", mspId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BillingServiceException("Failed to activate MSP", HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Unexpected error while activating MSP {}", mspId, e);
            throw new BillingServiceException("Unexpected error calling registration service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Activates the appropriate entity (MSP or Client Admin) after successful payment.
     * Handles both MSP and CLIENT role types with proper error handling.
     * 
     * @param payment The payment object containing invoiceId, clientId, mspAdminId
     * @param isFullyPaid Whether the invoice is fully paid
     * @param invoice Optional invoice object (if already fetched, pass to avoid duplicate fetch)
     * @param paymentId Optional payment ID for logging (defaults to payment.getId())
     */
    private void activateEntityAfterPayment(Payment payment, boolean isFullyPaid, Invoice invoice, String paymentId) {
        if (!isFullyPaid) {
            String logPaymentId = paymentId != null ? paymentId : payment.getId();
            log.info("Invoice partially paid. Activation not triggered. PaymentId: {}, InvoiceId: {}", 
                logPaymentId, payment.getInvoiceId());
            return;
        }
        
        // Fetch invoice if not provided
        Invoice activationInvoice = invoice;
        if (activationInvoice == null) {
            activationInvoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
        }
        
        if (activationInvoice == null) {
            String logPaymentId = paymentId != null ? paymentId : payment.getId();
            log.warn("Invoice not found for activation. PaymentId: {}, InvoiceId: {}", 
                logPaymentId, payment.getInvoiceId());
            return;
        }
        
        String logPaymentId = paymentId != null ? paymentId : payment.getId();
        
        // Handle MSP activation
        if (activationInvoice.getRoleType() == RoleType.MSP) {
            String mspId = payment.getMspAdminId() != null ? payment.getMspAdminId() : activationInvoice.getMspAdminId();
            if (mspId != null) {
                try {
                    activateMsp(mspId, payment.getInvoiceId());
                    log.info("MSP activated. PaymentId: {}, InvoiceId: {}, MspId: {}",
                        logPaymentId, payment.getInvoiceId(), mspId);
                } catch (Exception e) {
                    log.error("Failed to activate MSP {} after payment: {}", mspId, e.getMessage(), e);
                    // Don't fail payment processing if activation fails
                }
            } else {
                log.warn("MSP ID not found for invoice {}. Cannot activate MSP.", payment.getInvoiceId());
            }
        } 
        // Handle CLIENT activation
        else if (activationInvoice.getRoleType() == RoleType.CLIENT) {
            String clientId = payment.getClientId() != null ? payment.getClientId() : activationInvoice.getClientAdminId();
            if (clientId != null) {
                try {
                    activateClientAdmin(clientId);
                    log.info("Client admin activated. PaymentId: {}, InvoiceId: {}, ClientId: {}", 
                        logPaymentId, payment.getInvoiceId(), clientId);
                } catch (Exception e) {
                    log.error("Failed to activate client admin {} after payment: {}", clientId, e.getMessage(), e);
                    // Don't fail payment processing if activation fails
                }
            } else {
                log.warn("Client ID not found for invoice {}. Cannot activate client admin.", payment.getInvoiceId());
            }
        } else {
            log.warn("Unknown role type {} for invoice {}. Cannot activate entity.", 
                activationInvoice.getRoleType(), payment.getInvoiceId());
        }
    }






    /**
     * This method is used to process PayPal webhook events.
     *
     * @param payload
     * @param headers
     * @return
     */
    @Override
    public WebhookResponseDTO processPaypalWebhook(String payload, HttpHeaders headers) {
        try {
            JsonObject eventJson = JsonParser.parseString(payload).getAsJsonObject();
            String eventType = eventJson.get("event_type").getAsString();
            log.info("PayPal Webhook Received: {}", eventType);

            if ("PAYMENT.CAPTURE.COMPLETED".equals(eventType)) {
                JsonObject resource = eventJson.get("resource").getAsJsonObject();

                String transactionId = resource.get("id").getAsString();
                String paymentStatus = resource.get("status").getAsString(); // Expected: "COMPLETED"
                String currency = resource.getAsJsonObject("amount").get("currency_code").getAsString();
                String customId = resource.has("custom_id") ? resource.get("custom_id").getAsString() : null;

                if (customId == null || !customId.contains(":")) {
                    log.warn("Invalid or missing custom_id in PayPal resource.");
                    return new WebhookResponseDTO("error", "Missing or malformed custom_id", null, null);
                }

                String[] parts = customId.split(":");
                if (parts.length != 2) {
                    log.warn("Malformed custom_id format. Expected format 'PAYMENT_TYPE:ID', got: {}", customId);
                    return new WebhookResponseDTO("error", "Malformed custom_id", null, null);
                }

                String paymentType = parts[0];  // PRODUCT_PAYMENT or CREDIT_PAYMENT
                String identifier = parts[1];   // paymentId or creditTransactionId

                log.info("Captured PayPal Transaction - Type: {}, ID: {}, Status: {}, Currency: {}", paymentType, identifier, paymentStatus, currency);

                if ("CREDIT_PAYMENT".equalsIgnoreCase(paymentType)) {
                    // 🔍 Log only for credit payments (actual update logic can be added later)
                    log.info("✅ CREDIT_PAYMENT received via PayPal. Transaction ID: {}, CreditTransaction ID: {}", transactionId, identifier);

                    // You can later call creditService.updateCreditTransactionStatus(identifier, TransactionStatus.SUCCESS);

                    return new WebhookResponseDTO("success", "Credit payment processed (log only)", transactionId, identifier);
                }

                if ("PRODUCT_PAYMENT".equalsIgnoreCase(paymentType)) {
                    Payment payment = paymentRepository.findById(identifier).orElse(null);
                    if (payment == null) {
                        log.warn("No product payment found for ID: {}", identifier);
                        return new WebhookResponseDTO("error", "Product payment not found", null, identifier);
                    }

                    if ("COMPLETED".equalsIgnoreCase(paymentStatus)) {
                        payment.setStatus("SUCCESS");
                        payment.setTransactionId(transactionId);
                        paymentRepository.save(payment);

                        // Update invoice status based on total paid amount (PARTIAL or PAID)
                        boolean isFullyPaid = updateInvoiceStatusBasedOnPayments(payment.getInvoiceId(), payment);
                        log.info("Invoice status updated. InvoiceId: {}, FullyPaid: {}", payment.getInvoiceId(), isFullyPaid);
                        
                        // Send payment success notification to billing email
                        try {
                            Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
                            if (invoice != null) {
                                paymentNotificationUtil.sendPaymentSuccessNotification(payment, invoice);
                            } else {
                                log.warn("Invoice not found for payment success notification. InvoiceId: {}", payment.getInvoiceId());
                            }
                        } catch (Exception e) {
                            log.error("Failed to send payment success notification: {}", e.getMessage(), e);
                        }
                        
                        // Only activate client admin or MSP if invoice is fully paid
                        activateEntityAfterPayment(payment, isFullyPaid, null, identifier);

                        log.info("✅ PRODUCT_PAYMENT processed. Invoice updated for ID: {}", identifier);
                        return new WebhookResponseDTO("success", "Product payment processed", transactionId, identifier);
                    } else {
                        log.warn("Unexpected PayPal payment status: {}", paymentStatus);
                        return new WebhookResponseDTO("ignored", "Unhandled status: " + paymentStatus, transactionId, identifier);
                    }
                }

                log.warn("Unknown payment type: {}", paymentType);
                return new WebhookResponseDTO("ignored", "Unsupported payment type: " + paymentType, transactionId, identifier);
            }

            log.info("Unhandled PayPal event type: {}", eventType);
            return new WebhookResponseDTO("ignored", "Event not handled: " + eventType, null, null);

        } catch (Exception e) {
            log.error("Error processing PayPal webhook: {}", e.getMessage(), e);
            return new WebhookResponseDTO("error", "Webhook processing failed", null, null);
        }
    }



    /**
     * Helper method to calculate bill using invoice data directly.
     * This method is used when PaymentRequestDTO doesn't have packageItems and clientCountryId.
     *
     * @param packageItems Product selections from invoice
     * @param clientCountryId Client country ID (UUID format) from invoice
     * @param discountPercentage Discount percentage from invoice (optional, for fallback discount)
     * @param coupon Coupon to apply (optional)
     * @return Calculated bill response
     */
    private CouponDiscountResponseDTO calculateBillWithInvoiceData(
            List<ProductSelectionDto> packageItems,
            String clientCountryId,
            Double discountPercentage,
            CouponCreateResponseDTO coupon) {
        
        if (packageItems == null || packageItems.isEmpty()) {
            throw new IllegalArgumentException("Package items are required for bill calculation.");
        }

        boolean applyDiscountToAll = false;
        boolean applyFallbackDiscount = false;
        double fallbackDiscountRate = 0.0;

        // Coupon logic
        if (coupon != null) {
            if (coupon.getGeographicRestrictions() != null && !coupon.getGeographicRestrictions().isEmpty()) {
                if (clientCountryId != null && coupon.getGeographicRestrictions().contains(clientCountryId)) {
                    applyDiscountToAll = true;
                }
            }

            List<String> eligibleProductIds = coupon.getProductRestrictions() != null
                    ? coupon.getProductRestrictions().stream().map(ProductRestrictionDTO::getProductId).collect(Collectors.toList())
                    : null;

            if (!applyDiscountToAll && (eligibleProductIds == null || eligibleProductIds.isEmpty()) &&
                    (coupon.getGeographicRestrictions() == null || coupon.getGeographicRestrictions().isEmpty())) {
                applyDiscountToAll = true;
            }
        } else if (discountPercentage != null && discountPercentage > 0) {
            // Only applies if no coupon
            applyFallbackDiscount = true;
            fallbackDiscountRate = discountPercentage;
        }

        double actualTotal = 0.0;
        double totalDiscount = 0.0;
        List<DiscountedItemDTO> breakdown = new ArrayList<>();

        for (ProductSelectionDto item : packageItems) {
            double original = MoneyUtil.round(item.getLicenseCount() * item.getPricePerLicense() * item.getValidityPeriod());
            double discounted = original;
            boolean applied = false;

            if (coupon != null &&
                    (applyDiscountToAll || (coupon.getProductRestrictions() != null &&
                            coupon.getProductRestrictions().stream().anyMatch(pr -> pr.getProductId().equals(item.getProductId()))))) {

                if ("PERCENTAGE".equalsIgnoreCase(coupon.getType())) {
                    discounted = MoneyUtil.round(original - (original * coupon.getValue() / 100.0));
                } else if ("FIXED".equalsIgnoreCase(coupon.getType())) {
                    discounted = MoneyUtil.round(Math.max(0, original - coupon.getValue()));
                }
                applied = true;

            } else if (applyFallbackDiscount) {
                discounted = MoneyUtil.round(original - (original * fallbackDiscountRate / 100.0));
                applied = true;
            }

            totalDiscount = MoneyUtil.round(totalDiscount + (original - discounted));
            actualTotal = MoneyUtil.round(actualTotal + original);

            // Use productId for breakdown since coupon discounts are product-based, not package-based
            breakdown.add(new DiscountedItemDTO(item.getProductId(), original, discounted, applied));
        }

        double subtotal = MoneyUtil.round(actualTotal - totalDiscount);

        return new CouponDiscountResponseDTO(
                actualTotal,
                totalDiscount,
                subtotal,
                0.0,              // VAT excluded here
                subtotal,         // Grand total = subtotal (will be used further)
                breakdown
        );
    }


    /**
     * This method is used to calculate the bill based on the payment request and coupon.
     * This method is deprecated - use calculateBillWithInvoiceData() instead.
     * Maintained for backward compatibility only.
     *
     * @param paymentRequestDTO Payment request DTO (deprecated - packageItems should come from invoice)
     * @param coupon Optional coupon to apply
     * @return Calculated bill response
     * @deprecated Use calculateBillWithInvoiceData() with invoice data instead
     */
    @Override
    public CouponDiscountResponseDTO calculateBill(PaymentRequestDTO paymentRequestDTO, CouponCreateResponseDTO coupon) {
        // This method should not be called with the new PaymentRequestDTO structure
        // If called, it means packageItems are not available, which is an error
        throw new UnsupportedOperationException(
                "calculateBill(PaymentRequestDTO, CouponCreateResponseDTO) is deprecated. " +
                "Use calculateBillWithInvoiceData() with invoice data instead. " +
                "PaymentRequestDTO no longer contains packageItems, clientCountryId, or discountPercentage.");
    }

    /**
     * This method is used to calculate the bill without a coupon.
     *
     * @param paymentRequestDTO
     * @return
     */
    @Override
    public CouponDiscountResponseDTO calculateBill(PaymentRequestDTO paymentRequestDTO) {
        return calculateBill(paymentRequestDTO, null);
    }

    @Override
    public List<PaymentHistoryItemDTO> getPaymentHistory(List<String> statuses, String method, String startDateStr,
            String endDateStr, String clientId, String mspId, String countryId, RoleType roleType, String search,
            int offset, int limit) {

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        Instant startDate = parseInstant(startDateStr);
        Instant endDate = parseInstantEod(endDateStr);

        if (limit <= 0) {
            throw new BillingServiceException(
                "Limit must be greater than 0",
                HttpStatus.BAD_REQUEST
            );
        }

        if (offset < 0) {
            throw new BillingServiceException(
                "Offset must be greater than or equal to 0",
                HttpStatus.BAD_REQUEST
            );
        }

        int skip = offset * limit;
        List<Payment> payments = paymentRepositoryCustom.findPaymentsWithDynamicFilters(
                statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search, skip, limit);

        List<String> invoiceIds = payments.stream()
                .map(Payment::getInvoiceId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        java.util.Map<String, Invoice> invoiceMap = new java.util.HashMap<>();
        if (!invoiceIds.isEmpty()) {
            List<Invoice> invoices = invoiceRepository.findAllById(invoiceIds);
            invoiceMap = invoices.stream()
                    .collect(java.util.stream.Collectors.toMap(Invoice::getId, inv -> inv));
        }

        final java.util.Map<String, Invoice> finalInvoiceMap = invoiceMap;

        return payments.stream()
                .map(payment -> toPaymentHistoryItem(payment, finalInvoiceMap.get(payment.getInvoiceId())))
                .toList();
    }

    @Override
    public long countPaymentHistory(List<String> statuses, String method, String startDateStr, String endDateStr,
            String clientId, String mspId, String countryId, RoleType roleType, String search) {
        Instant startDate = parseInstant(startDateStr);
        Instant endDate = parseInstantEod(endDateStr);

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        return paymentRepositoryCustom.countPaymentsWithDynamicFilters(
                statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);
    }

    @Override
    public String generatePaymentHistoryExcel(List<String> statuses, String method, String startDate, String endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search) {

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        List<PaymentHistoryItemDTO> historyList = getPaymentHistory(
                statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search, 0, Integer.MAX_VALUE);

        ByteArrayOutputStream excelOutput = ExcelGeneratorUtil.generatePaymentHistoryExcel(historyList);

        String fileName = "payment-history-" + System.currentTimeMillis() + ".xlsx";

        return azureInvoiceUploader.uploadInvoice(
                "payment-history/" + fileName,
                new ByteArrayInputStream(excelOutput.toByteArray()),
                excelOutput.size(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }


    @Override
    public PaymentHistorySummaryDTO getPaymentHistorySummary(String startDateStr, String endDateStr, String clientId, String mspId, String countryId, RoleType roleType) {
        Instant startDate = parseInstant(startDateStr);
        Instant endDate = parseInstantEod(endDateStr);

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set mspId from Current Context whether UserType
        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }
        // Get current period summary
        PaymentSummaryResult currentSummary = paymentRepositoryCustom.getPaymentSummary(startDate, endDate, clientId, mspId, countryId, roleType);

        // Calculate previous period date range for comparison
        LocalDate endLocalDate = endDate != null ? 
            endDate.atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now();
        LocalDate startLocalDate = startDate != null ? 
            startDate.atZone(ZoneId.systemDefault()).toLocalDate() : 
            endLocalDate.minusMonths(1);

        // Calculate period duration and validate
        long daysBetween = ChronoUnit.DAYS.between(startLocalDate, endLocalDate);
        if (daysBetween <= 0) {
            // Invalid period, default to 30 days
            log.warn("Invalid date range detected (daysBetween: {}), defaulting to 30 days", daysBetween);
            daysBetween = 30;
            startLocalDate = endLocalDate.minusDays(30);
        }

        // Previous period: same duration, ending one day before current period starts
        LocalDate prevEndDate = startLocalDate.minusDays(1);
        LocalDate prevStartDate = prevEndDate.minusDays(daysBetween - 1); // -1 to include both start and end days

        Instant prevStart = prevStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant prevEnd = prevEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Get previous period summary
        PaymentSummaryResult previousSummary = paymentRepositoryCustom.getPaymentSummary(prevStart, prevEnd, clientId, mspId, countryId, roleType);

        // Calculate percentage changes with null safety
        double previousTotalPayments = previousSummary != null && previousSummary.getTotalPayments() != null ? 
            previousSummary.getTotalPayments() : 0.0;
        double currentTotalPayments = currentSummary != null && currentSummary.getTotalPayments() != null ? 
            currentSummary.getTotalPayments() : 0.0;
        double previousOutstanding = previousSummary != null && previousSummary.getOutstandingAmount() != null ? 
            previousSummary.getOutstandingAmount() : 0.0;
        double currentOutstanding = currentSummary != null && currentSummary.getOutstandingAmount() != null ? 
            currentSummary.getOutstandingAmount() : 0.0;
        
        double totalPaymentsChange = calculatePercentageChange(previousTotalPayments, currentTotalPayments);
        double outstandingAmountChange = calculatePercentageChange(previousOutstanding, currentOutstanding);

        // Build response with null safety
        return PaymentHistorySummaryDTO.builder()
                .totalPayments(currentSummary != null ? currentSummary.getTotalPayments() : 0.0)
                .totalPaymentsChange(totalPaymentsChange)
                .outstandingAmount(currentSummary != null && currentSummary.getOutstandingAmount() != null ? 
                    currentSummary.getOutstandingAmount() : 0.0)
                .outstandingAmountChange(outstandingAmountChange)
                .paidInvoicesCount(currentSummary != null && currentSummary.getPaidInvoicesCount() != null ? 
                    currentSummary.getPaidInvoicesCount() : 0L)
                .totalInvoicesCount(currentSummary != null && currentSummary.getTotalInvoicesCount() != null ? 
                    currentSummary.getTotalInvoicesCount() : 0L)
                .overdueInvoicesCount(currentSummary != null && currentSummary.getOverdueInvoicesCount() != null ? 
                    currentSummary.getOverdueInvoicesCount() : 0L)
                .build();
    }

    private double calculatePercentageChange(double previous, double current) {
        if (previous == 0.0) {
            // If previous was 0 and current > 0, it's a new value (infinite % increase)
            // Return -1 as a sentinel value to indicate "new value" or "not applicable"
            // This is clearly distinguishable from valid percentage changes
            return current > 0 ? -1.0 : 0.0;
        }
        if (current == 0.0 && previous > 0) {
            // If current is 0 and previous > 0, it's a 100% decrease
            return -100.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    @Override
    public byte[] generatePaymentHistoryCsv(List<String> statuses, String method, String startDate, String endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search) {

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        List<PaymentHistoryItemDTO> historyList = getPaymentHistory(
                statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search, 0, Integer.MAX_VALUE);

        ByteArrayOutputStream csvOutput = CsvGeneratorUtil.generatePaymentHistoryCsv(historyList);
        return csvOutput.toByteArray();
    }

    @Override
    public PaymentSummaryReportDTO getPaymentSummaryReport(String search, String clientAdminId, String mspId,
            List<String> statuses, String method, String startDate, String endDate, QuickRange quickRange) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        mspId = forceMspIdIfNeeded(mspId);

        Instant start = parseInstant(dates[0]);
        Instant end = parseInstantEod(dates[1]);

        Map<String, PaymentStatusAggregate> aggregates = paymentRepositoryCustom.aggregatePaymentsByStatus(
                method, start, end, clientAdminId, mspId, null, null, search, statuses);

        PaymentStatusAggregate success = aggregates.getOrDefault("SUCCESS", emptyAggregate());
        PaymentStatusAggregate pending = aggregates.getOrDefault("PENDING", emptyAggregate());
        PaymentStatusAggregate failed = aggregates.getOrDefault("FAILED", emptyAggregate());
        PaymentStatusAggregate cancelled = aggregates.getOrDefault("CANCELLED", emptyAggregate());

        long totalCount = aggregates.values().stream().mapToLong(PaymentStatusAggregate::getCount).sum();

        return PaymentSummaryReportDTO.builder()
                .totalPaymentCount(totalCount)
                .successPaymentCount(success.getCount())
                .pendingPaymentCount(pending.getCount())
                .failedPaymentCount(failed.getCount())
                .cancelledPaymentCount(cancelled.getCount())
                .totalSuccessAmount(success.getAmountSum())
                .totalPendingAmount(pending.getAmountSum())
                .totalFailedAmount(failed.getAmountSum())
                .totalCancelledAmount(cancelled.getAmountSum())
                .build();
    }

    @Override
    public List<PaymentHistoryItemDTO> getPaymentSummaryReportList(String search, String clientAdminId, String mspId,
            List<String> statuses, String method, String startDate, String endDate, QuickRange quickRange,
            int offset, int limit) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        return getPaymentHistory(statuses, method, dates[0], dates[1], clientAdminId, mspId, null, null, search,
                offset, limit);
    }

    @Override
    public long countPaymentSummaryReport(String search, String clientAdminId, String mspId, List<String> statuses,
            String method, String startDate, String endDate, QuickRange quickRange) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        return countPaymentHistory(statuses, method, dates[0], dates[1], clientAdminId, mspId, null, null, search);
    }

    @Override
    public byte[] generatePaymentSummaryReportCsv(String search, String clientAdminId, String mspId,
            List<String> statuses, String method, String startDate, String endDate, QuickRange quickRange) {
        String[] dates = QuickRangeResolver.resolve(quickRange, startDate, endDate);
        List<PaymentHistoryItemDTO> items = getPaymentHistory(
                statuses, method, dates[0], dates[1], clientAdminId, mspId, null, null, search, 0, Integer.MAX_VALUE);
        return CsvGeneratorUtil.generatePaymentHistoryCsv(items).toByteArray();
    }

    private String forceMspIdIfNeeded(String mspId) {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (UserType.MSP.name().equals(userContext.getUserType())) {
            return userContext.getUserId();
        }
        return mspId;
    }

    private static PaymentStatusAggregate emptyAggregate() {
        return PaymentStatusAggregate.builder().count(0L).amountSum(0.0).build();
    }

    @Override
    public PaymentDetailsDTO getPaymentDetails(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                .orElse(null); // Invoice is optional

        double subtotal = invoice != null ? invoice.getSubtotal() : payment.getSubtotal();
        double discountAmount = invoice != null ? invoice.getDiscountAmount() : payment.getDiscountAmount();
        double vatAmount = invoice != null ? invoice.getVatAmount() : payment.getVatAmount();

        return PaymentDetailsDTO.builder()
                // From Payment
                .paymentId(payment.getId())
                .invoiceId(payment.getInvoiceId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .online(payment.isOnline())
                .isActive(payment.isActive())
                .clientId(payment.getClientId())
                .notes(payment.getNotes())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .couponId(invoice != null ? invoice.getCouponId() : payment.getCouponId())
                .couponCode(invoice != null ? invoice.getCouponCode() : payment.getCouponCode())
                .discountAmount(discountAmount)
                .actualAmount(payment.getActualAmount())
                .subtotal(subtotal)
                .vatAmount(vatAmount)
                .transactionId(payment.getTransactionId())
                .metaData(payment.getMetaData())
                .breakdown(payment.getBreakdown())
                .paymentSources(payment.getPaymentSources())
                .invoiceFileKey(payment.getInvoiceFileKey())
                .receiptGenerated(payment.isReceiptGenerated())

                // From Invoice
                .clientAdminId(invoice != null ? invoice.getClientAdminId() : null)
                .clientName(invoice != null ? invoice.getClientName() : null)
                .clientProductIds(invoice != null ? invoice.getClientProductIds() : null)
                .invoiceSubtotal(invoice != null ? invoice.getSubtotal() : 0.0)
                .invoiceDiscountAmount(invoice != null ? invoice.getDiscountAmount() : 0.0)
                .invoiceVatAmount(invoice != null ? invoice.getVatAmount() : 0.0)
                .totalAmount(invoice != null ? invoice.getTotalAmount() : 0.0)
                .statusNote(invoice != null ? invoice.getStatusNote() : null)
                .invoiceStatus(invoice != null ? invoice.getStatus() : null)
                .invoicePdfLink(invoice != null ? invoice.getInvoicePdfLink() : null)
                .invoiceCreatedAt(invoice != null ? invoice.getCreatedAt() : null)
                .paidAt(invoice != null ? invoice.getPaidAt() : null)
                .productSelections(invoice != null ? invoice.getProductSelections() : null)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseDTO payForUsedCredit(UsedCreditPaymentRequestDTO request) {
        String method = request.getMethod().toUpperCase();
        String clientId = request.getClientId();

        // Step 1: Validate amount and input
        if (request.getAmount() <= 0) {
            throw new BillingServiceException("Amount must be greater than zero", HttpStatus.BAD_REQUEST);
        }
        if (request.getCreditTransactionId() == null || request.getCreditTransactionId().isBlank()) {
            throw new BillingServiceException("Missing creditTransactionId to update", HttpStatus.BAD_REQUEST);
        }

        // Step 2: Initialize variables
        String transactionId;
        String checkoutUrl = null;
        TransactionStatus status;

        // Step 3: Handle payment method
        if ("STRIPE".equals(method)) {
            GatewayResponseDTO stripeRes = processStripeForCredit(request);
            transactionId = stripeRes.getTransactionId();
            checkoutUrl = stripeRes.getCheckoutUrl();
            request.setReferenceId(transactionId);
            status = TransactionStatus.ONPROGRESS;
        } else if ("PAYPAL".equals(method)) {
            GatewayResponseDTO paypalRes = processPaypalForCredit(request);
            transactionId = paypalRes.getTransactionId();
            checkoutUrl = paypalRes.getCheckoutUrl();
            request.setReferenceId(transactionId);
            status = TransactionStatus.ONPROGRESS;
        } else if ("MANUAL".equals(method)) {
            transactionId = "manual-" + System.currentTimeMillis();
            request.setReferenceId(transactionId);
            status = TransactionStatus.PENDING;
        } else {
            throw new BillingServiceException("Unsupported payment method: " + method, HttpStatus.BAD_REQUEST);
        }

        // Step 4: Update credit transaction
        CreditTransactionUpdateRequestDTO updateDto = new CreditTransactionUpdateRequestDTO();
        updateDto.setTransactionId(request.getCreditTransactionId());
        updateDto.setReferenceId(request.getReferenceId());
        updateDto.setRemarks(request.getRemarks());
        updateDto.setStatus(status);
        creditService.updateCreditTransaction(updateDto);

        // Step 5: Save payment entry
        // Get invoice to extract mspAdminId and countryId
        Invoice invoice = null;
        if (request.getInvoiceId() != null && !request.getInvoiceId().isBlank()) {
            invoice = invoiceRepository.findById(request.getInvoiceId()).orElse(null);
        }
        
        Payment payment = new Payment();
        String paymentId = UUID.randomUUID().toString();
        payment.setId(paymentId);
        payment.setClientId(clientId);
        payment.setInvoiceId(request.getInvoiceId());
        // Set mspAdminId, countryId, and roleType from Invoice if available
        payment.setMspAdminId(invoice != null ? invoice.getMspAdminId() : null);
        payment.setCountryId(invoice != null ? invoice.getCountryId() : null);
        payment.setRoleType(invoice != null ? invoice.getRoleType() : null);
        payment.setPaymentDate(Instant.now());
        payment.setNotes(request.getRemarks());
        payment.setCurrency("usd");
        payment.setStatus("PENDING");
        payment.setOnline(!"MANUAL".equalsIgnoreCase(request.getMethod()));
        payment.setActive(true);
        payment.setCreatedAt(Instant.now());
        payment.setAmount(request.getAmount());
        payment.setSubtotal(request.getAmount());
        payment.setVatAmount(0.0);
        payment.setDiscountAmount(0.0);
        payment.setTransactionId(request.getCreditTransactionId());
        payment.setBreakdown(null);

        PaymentSourceDTO source = new PaymentSourceDTO();
        source.setMethod(request.getMethod());
        source.setAmount(request.getAmount());
        source.setTransactionId(request.getReferenceId());
        payment.setPaymentSources(List.of(source));

        payment.setPaymentType(PaymentType.CREDIT_PAYMENT);
        payment = paymentRepository.save(payment);

        // Step 6: Return response
        return new PaymentResponseDTO(
                request.getCreditTransactionId(),
                checkoutUrl,
                method + " payment initiated successfully"
        );
    }

    private GatewayResponseDTO processStripeForCredit(UsedCreditPaymentRequestDTO request) {
        Stripe.apiKey = stripeSecretKey;

        String generatedPaymentId = "pay-credit-" + System.currentTimeMillis();

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontEndUrl + "/payment-success?amount=" + request.getAmount()
                        + "&Transactionid=" + generatedPaymentId
                        + "&orderNumber=" + request.getCreditTransactionId())
                .setCancelUrl(frontEndUrl + "/payment-failed");

        paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(MoneyUtil.toCents(request.getAmount()))
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Credit Payment: " + request.getCreditTransactionId())
                                .build())
                        .build())
                .build());

        paramsBuilder.setPaymentIntentData(
                SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("paymentId", generatedPaymentId)
                        .putMetadata("clientId", request.getClientId())
                        .putMetadata("amount", String.valueOf(request.getAmount()))
                        .putMetadata("paymentType", "CREDIT_PAYMENT")
                        .putMetadata("creditTransactionId", request.getCreditTransactionId())
                        .build()
        );

        try {
            Session session = Session.create(paramsBuilder.build());
            return new GatewayResponseDTO(session.getId(), session.getUrl(), "success", "Stripe credit session created");
        } catch (StripeException e) {
            throw new BillingServiceException("Stripe session creation failed: " + e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private GatewayResponseDTO processPaypalForCredit(UsedCreditPaymentRequestDTO request) {
        try {
            PayPalEnvironment environment = paypalMode.equalsIgnoreCase("sandbox")
                    ? new PayPalEnvironment.Sandbox(paypalClientId, paypalSecret)
                    : new PayPalEnvironment.Live(paypalClientId, paypalSecret);

            PayPalHttpClient client = new PayPalHttpClient(environment);

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            ApplicationContext appContext = new ApplicationContext()
                    .brandName("Aspire LMS")
                    .landingPage("LOGIN")
                    .cancelUrl(frontEndUrl + "/payment-failed")
                    .returnUrl(frontEndUrl + "/payment-success?amount=" + request.getAmount()
                            + "&Transactionid=" + request.getCreditTransactionId()
                            + "&source=paypal")
                    .userAction("PAY_NOW");

            orderRequest.applicationContext(appContext);

            AmountWithBreakdown amount = new AmountWithBreakdown()
                    .currencyCode("USD")
                    .value(String.format("%.2f", MoneyUtil.round(request.getAmount())));

            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .description("Credit Transaction ID: " + request.getCreditTransactionId())
                    .customId("CREDIT_PAYMENT:" + request.getCreditTransactionId())
                    .amountWithBreakdown(amount);

            orderRequest.purchaseUnits(List.of(purchaseUnit));

            OrdersCreateRequest apiRequest = new OrdersCreateRequest();
            apiRequest.prefer("return=representation");
            apiRequest.requestBody(orderRequest);

            HttpResponse<Order> response = client.execute(apiRequest);
            Order order = response.result();

            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equalsIgnoreCase(link.rel()))
                    .map(LinkDescription::href)
                    .findFirst()
                    .orElseThrow(() -> new BillingServiceException("No approval URL found in PayPal response", HttpStatus.BAD_REQUEST));

            return new GatewayResponseDTO(order.id(), approvalUrl, "success", "PayPal credit session created");
        } catch (Exception e) {
            throw new BillingServiceException("PayPal session creation failed: " + e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    private Instant parseInstant(String str) {
        if (str == null || str.isBlank()) return null;
        return LocalDate.parse(str).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant parseInstantEod(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }

        return LocalDate.parse(str)
                .atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    void populatePaymentFinancialsFromInvoice(Payment payment, Invoice invoice) {
        payment.setSubtotal(MoneyUtil.round(invoice.getSubtotal()));
        payment.setVatAmount(MoneyUtil.round(invoice.getVatAmount()));
        payment.setDiscountAmount(MoneyUtil.round(invoice.getDiscountAmount() + invoice.getCouponDiscountAmount()));
        payment.setCouponCode(invoice.getCouponCode());
        payment.setCouponId(invoice.getCouponId());
        payment.setBreakdown(null);
    }

    PaymentHistoryItemDTO toPaymentHistoryItem(Payment payment, Invoice invoice) {
        double totalAmount = invoice != null ? MoneyUtil.round(invoice.getTotalAmount()) : 0.0;
        double amount = payment.getAmount() != null ? MoneyUtil.round(payment.getAmount()) : 0.0;
        double outstanding = MoneyUtil.round(Math.max(0, totalAmount - amount));

        return PaymentHistoryItemDTO.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoiceId())
                .invoiceNumber(payment.getInvoiceId())
                .date(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null)
                .amount(payment.getAmount())
                .dueAmount(calculateDue(payment))
                .status(payment.getStatus())
                .paymentMethod(resolvePaymentMethod(payment))
                .clientId(payment.getClientId())
                .clientName(invoice != null ? invoice.getClientName() : null)
                .mspId(payment.getMspAdminId())
                .mspName(invoice != null ? invoice.getMspName() : null)
                .countryId(payment.getCountryId())
                .countryName(invoice != null ? invoice.getCountryName() : null)
                .roleType(payment.getRoleType())
                .invoiceDate(invoice != null ? invoice.getCreatedAt() : null)
                .totalAmount(totalAmount)
                .outstanding(outstanding)
                .discountAmount(invoice != null ? invoice.getDiscountAmount() : payment.getDiscountAmount())
                .discountType(invoice != null && invoice.getDiscountType() != null ? invoice.getDiscountType().name() : null)
                .discountPercentage(invoice != null ? invoice.getDiscountPercentage() : null)
                .couponCode(invoice != null && invoice.getCouponCode() != null ? invoice.getCouponCode() : payment.getCouponCode())
                .couponDiscountAmount(invoice != null ? invoice.getCouponDiscountAmount() : null)
                .actualAmount(payment.getActualAmount())
                .subtotal(invoice != null ? invoice.getSubtotal() : payment.getSubtotal())
                .vatAmount(invoice != null ? invoice.getVatAmount() : payment.getVatAmount())
                .currency(payment.getCurrency())
                .transactionId(payment.getTransactionId())
                .paymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : null)
                .build();
    }

    private String resolvePaymentMethod(Payment payment) {
        if (payment.getPaymentSources() == null || payment.getPaymentSources().isEmpty()) return "Unknown";
        return payment.getPaymentSources().get(0).getMethod(); // assuming first method is primary
    }

    private double calculateDue(Payment payment) {
        return payment.getActualAmount() != null && payment.getAmount() != null
                ? Math.max(0, payment.getActualAmount() - payment.getAmount())
                : 0.0;
    }


    /**
     * This method is used to process Stripe payment.
     *
     * @param gatewayRequest
     * @return
     */
    private GatewayResponseDTO processStripePayment(PaymentGatewayRequestDTO gatewayRequest) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontEndUrl + "/payment-success?amount=" + gatewayRequest.getAmount() + "&Transactionid=" + gatewayRequest.getPaymentId() + "&orderNumber=" + gatewayRequest.getInvoiceId())
                .setCancelUrl(frontEndUrl + "/payment-failed");

        // Add line item
        paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(gatewayRequest.getCurrency())
                        .setUnitAmount(MoneyUtil.toCents(gatewayRequest.getAmount()))
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Invoice " + gatewayRequest.getInvoiceId())
                                .build())
                        .build())
                .build());

        // Correct way to pass metadata to PaymentIntent
        paramsBuilder.setPaymentIntentData(
                SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("paymentId", gatewayRequest.getPaymentId())
                        .putMetadata("invoiceId", gatewayRequest.getInvoiceId())
                        .putMetadata("clientId", gatewayRequest.getClientId())
                        .putMetadata("amount", String.valueOf(gatewayRequest.getAmount()))
                        .putMetadata("paymentType", "PRODUCT_PAYMENT")
                        .build()
        );

        Session session = Session.create(paramsBuilder.build());

        return new GatewayResponseDTO(session.getId(), session.getUrl(), "success", "Stripe Checkout Session Created Successfully");
    }


    /**
     * This method is used to process PayPal payment.
     *
     * @param gatewayRequest
     * @return
     */
    private GatewayResponseDTO processPaypalPayment(PaymentGatewayRequestDTO gatewayRequest) {
        try {
            // Set up environment (sandbox or live)
            PayPalEnvironment environment = paypalMode.equalsIgnoreCase("sandbox")
                    ? new PayPalEnvironment.Sandbox(paypalClientId, paypalSecret)
                    : new PayPalEnvironment.Live(paypalClientId, paypalSecret);

            PayPalHttpClient client = new PayPalHttpClient(environment);

            // Create order request
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            // Set application context for return and cancel URLs
            ApplicationContext applicationContext = new ApplicationContext()
                    .brandName("Aspire LMS")
                    .landingPage("LOGIN")
                    .cancelUrl(frontEndUrl + "/payment-failed")
                    .returnUrl(frontEndUrl + "/payment-success?amount=" + gatewayRequest.getAmount()
                            + "&Transactionid=" + gatewayRequest.getPaymentId()
                            + "&orderNumber=" + gatewayRequest.getInvoiceId())
                    .userAction("PAY_NOW");

            orderRequest.applicationContext(applicationContext);

            // Set amount and custom_id in purchase unit
            AmountWithBreakdown amount = new AmountWithBreakdown()
                    .currencyCode(gatewayRequest.getCurrency().toUpperCase())
                    .value(String.format("%.2f", MoneyUtil.round(gatewayRequest.getAmount())));

            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .description("Aspire LMS Invoice #" + gatewayRequest.getInvoiceId())
                    .customId("PRODUCT_PAYMENT:" + gatewayRequest.getPaymentId())
                    .amountWithBreakdown(amount);

            orderRequest.purchaseUnits(List.of(purchaseUnit));

            // Send order creation request
            OrdersCreateRequest request = new OrdersCreateRequest();
            request.prefer("return=representation");
            request.requestBody(orderRequest);

            HttpResponse<Order> httpResponse = client.execute(request);
            Order createdOrder = httpResponse.result();

            // Extract approval URL for frontend redirection
            String approvalUrl = createdOrder.links().stream()
                    .filter(link -> "approve".equalsIgnoreCase(link.rel()))
                    .map(LinkDescription::href)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Approval URL not found."));

            return new GatewayResponseDTO(
                    createdOrder.id(),
                    approvalUrl,
                    "success",
                    "PayPal checkout session created successfully."
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BillingServiceException("PayPal payment initiation failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method is used to refund credits if needed.
     *
     * @param payment
     */
    public void refundCreditsIfNeeded(Payment payment) {
        if (payment.getPaymentSources() != null && !payment.getPaymentSources().isEmpty()) {
            for (PaymentSourceDTO source : payment.getPaymentSources()) {
                if ("CREDIT".equalsIgnoreCase(source.getMethod())) {
                    Credit credit = creditRepository.findByClientId(payment.getClientId())
                            .orElseThrow(() -> new ResourceNotFoundException("Credit account not found for refund"));

                    // Refund the used credits
                    credit.setAvailableAmount(credit.getAvailableAmount() + source.getAmount());
                    credit.setUpdatedAt(Instant.now());
                    creditRepository.save(credit);

                    // Log reversal for used credits
                    CreditTransactionCreateDTO reversalTx = new CreditTransactionCreateDTO();
                    reversalTx.setClientId(payment.getClientId());
                    reversalTx.setType("REVERSAL");
                    reversalTx.setAmount(source.getAmount());
                    reversalTx.setReferenceType("PAYMENT_REFUND");
                    reversalTx.setReferenceId(payment.getId());
                    reversalTx.setDescription("Refunded credits due to failed payment for invoice " + payment.getInvoiceId());
                    reversalTx.setInitiatedBy("SYSTEM");

                    creditService.logCreditTransaction(reversalTx);
                }
            }
        }

        // Also reverse commission if added earlier
        List<CreditTransaction> commissionTxns = creditTransactionRepository.findByReferenceTypeAndReferenceId("COMMISSION", payment.getInvoiceId());

        for (CreditTransaction commissionTxn : commissionTxns) {
            if (!commissionTxn.isReversed()) {
                Credit credit = creditRepository.findByClientId(payment.getClientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Credit account not found for commission reversal"));

                // Deduct the deposited commission
                credit.setAvailableAmount(credit.getAvailableAmount() - commissionTxn.getAmount());
                credit.setUpdatedAt(Instant.now());
                creditRepository.save(credit);

                // Mark as reversed
                commissionTxn.setReversed(true);
                creditTransactionRepository.save(commissionTxn);

                // Log reversal transaction
                CreditTransactionCreateDTO reversalTx = new CreditTransactionCreateDTO();
                reversalTx.setClientId(payment.getClientId());
                reversalTx.setType("REVERSAL");
                reversalTx.setAmount(commissionTxn.getAmount());
                reversalTx.setReferenceType("COMMISSION_REVERSAL");
                reversalTx.setReferenceId(payment.getInvoiceId());
                reversalTx.setDescription("Reversal of commission for failed payment on invoice " + payment.getInvoiceId());
                reversalTx.setInitiatedBy("SYSTEM");

                creditService.logCreditTransaction(reversalTx);
            }
        }
    }



}


