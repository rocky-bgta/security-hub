package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.billing.service.PaymentReconciliationService;
import com.aspire.asat.billing.service.PaymentService;
import com.aspire.asat.common.util.MoneyUtil;
import com.stripe.Stripe;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service implementation for reconciling payments with Stripe API
 * to identify and process payments that succeeded but weren't processed locally
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentReconciliationServiceImpl implements PaymentReconciliationService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;
    
    @Value("${payment.reconciliation.lookback.days:7}")
    private int lookbackDays;
    
    @Value("${payment.reconciliation.batch.size:50}")
    private int batchSize;

    @Override
    public void reconcileStripePayments() {
        log.info("Starting Stripe payment reconciliation job");
        
        try {
            Stripe.apiKey = stripeSecretKey;
            
            // Calculate lookback date
            Instant lookbackDate = Instant.now().minus(lookbackDays, java.time.temporal.ChronoUnit.DAYS);
            
            // Query for PENDING online payments with transactionId within lookback period
            List<Payment> pendingPayments = paymentRepository.findPendingOnlinePaymentsWithTransactionIdSince(lookbackDate);
            
            if (pendingPayments.isEmpty()) {
                log.info("No pending online payments found for reconciliation");
                return;
            }
            
            log.info("Found {} pending online payments to reconcile", pendingPayments.size());
            
            int reconciled = 0;
            int errors = 0;
            int skipped = 0;
            
            // Process payments in batches to avoid overwhelming Stripe API
            for (int i = 0; i < pendingPayments.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, pendingPayments.size());
                List<Payment> batch = pendingPayments.subList(i, endIndex);
                
                log.debug("Processing batch {}-{} of {} payments", i + 1, endIndex, pendingPayments.size());
                
                for (Payment payment : batch) {
                    try {
                        ReconciliationResult result = reconcilePayment(payment);
                        switch (result) {
                            case RECONCILED:
                                reconciled++;
                                break;
                            case SKIPPED:
                                skipped++;
                                break;
                            case ERROR:
                                errors++;
                                break;
                        }
                    } catch (Exception e) {
                        log.error("Error reconciling payment {}: {}", payment.getId(), e.getMessage(), e);
                        errors++;
                    }
                }
                
                // Small delay between batches to respect rate limits
                if (endIndex < pendingPayments.size()) {
                    try {
                        Thread.sleep(100); // 100ms delay between batches
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Reconciliation interrupted");
                        break;
                    }
                }
            }
            
            log.info("Stripe payment reconciliation completed. Total: {}, Reconciled: {}, Skipped: {}, Errors: {}", 
                    pendingPayments.size(), reconciled, skipped, errors);
                    
        } catch (Exception e) {
            log.error("Fatal error in Stripe payment reconciliation: {}", e.getMessage(), e);
        }
    }

    /**
     * Reconciles a single payment by checking its status in Stripe
     * @param payment The payment to reconcile
     * @return ReconciliationResult indicating the outcome
     */
    private ReconciliationResult reconcilePayment(Payment payment) {
        String paymentId = payment.getId();
        String transactionId = payment.getTransactionId();
        
        if (transactionId == null || transactionId.trim().isEmpty()) {
            log.warn("Payment {} has no transactionId, skipping reconciliation", paymentId);
            return ReconciliationResult.SKIPPED;
        }
        
        // Check if payment is still PENDING (might have been updated by webhook)
        Payment currentPayment = paymentRepository.findById(paymentId).orElse(null);
        if (currentPayment == null) {
            log.warn("Payment {} not found in database, skipping", paymentId);
            return ReconciliationResult.SKIPPED;
        }
        
        if (!"PENDING".equalsIgnoreCase(currentPayment.getStatus())) {
            log.debug("Payment {} is no longer PENDING (status: {}), skipping reconciliation", 
                    paymentId, currentPayment.getStatus());
            return ReconciliationResult.SKIPPED;
        }
        
        try {
            // Retrieve PaymentIntent from Stripe
            PaymentIntent intent = retrievePaymentIntentWithRetry(transactionId);
            
            if (intent == null) {
                log.warn("PaymentIntent {} not found in Stripe for payment {}", transactionId, paymentId);
                return ReconciliationResult.ERROR;
            }
            
            String stripeStatus = intent.getStatus();
            log.debug("Payment {} has Stripe status: {}", paymentId, stripeStatus);
            
            // Check if payment succeeded in Stripe
            if ("succeeded".equalsIgnoreCase(stripeStatus)) {
                log.info("Reconciling payment {}: Stripe shows succeeded, updating local status", paymentId);
                
                // Verify amount matches (optional but recommended)
                Long stripeAmount = intent.getAmount();
                if (stripeAmount != null && payment.getAmount() != null) {
                    double stripeAmountDecimal = MoneyUtil.round(stripeAmount / 100.0);
                    double paymentAmount = MoneyUtil.round(payment.getAmount());
                    
                    if (!MoneyUtil.isEqual(stripeAmountDecimal, paymentAmount)) {
                        log.warn("Amount mismatch for payment {}: Stripe={}, Local={}", 
                                paymentId, stripeAmountDecimal, paymentAmount);
                        // Continue anyway, but log the discrepancy
                    }
                }
                
                // Update payment status using existing method
                // This will trigger invoice updates, notifications, and client activation
                paymentService.updatePaymentStatus(paymentId, "paid", transactionId);
                log.info("Successfully reconciled payment {} from PENDING to SUCCESS", paymentId);
                return ReconciliationResult.RECONCILED;
            } else if ("requires_action".equalsIgnoreCase(stripeStatus) || 
                       "requires_payment_method".equalsIgnoreCase(stripeStatus) ||
                       "requires_confirmation".equalsIgnoreCase(stripeStatus)) {
                log.debug("Payment {} is in intermediate state: {}, skipping", paymentId, stripeStatus);
                return ReconciliationResult.SKIPPED;
            } else if ("canceled".equalsIgnoreCase(stripeStatus) || 
                       "requires_capture".equalsIgnoreCase(stripeStatus)) {
                log.debug("Payment {} has status: {}, not reconciling", paymentId, stripeStatus);
                return ReconciliationResult.SKIPPED;
            } else {
                log.debug("Payment {} has unhandled Stripe status: {}, skipping", paymentId, stripeStatus);
                return ReconciliationResult.SKIPPED;
            }
            
        } catch (RateLimitException e) {
            log.warn("Rate limit exceeded while reconciling payment {}, will retry in next run: {}", 
                    paymentId, e.getMessage());
            return ReconciliationResult.ERROR;
        } catch (StripeException e) {
            log.error("Stripe API error while reconciling payment {}: {}", paymentId, e.getMessage(), e);
            return ReconciliationResult.ERROR;
        }
    }

    /**
     * Retrieves a PaymentIntent from Stripe with retry logic for rate limits
     */
    private PaymentIntent retrievePaymentIntentWithRetry(String paymentIntentId) throws StripeException {
        int maxRetries = 3;
        int retryDelayMs = 1000; // Start with 1 second
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return PaymentIntent.retrieve(paymentIntentId);
            } catch (RateLimitException e) {
                if (attempt < maxRetries) {
                    log.warn("Rate limit hit (attempt {}/{}), retrying in {}ms", 
                            attempt, maxRetries, retryDelayMs);
                    try {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", e);
                    }
                } else {
                    throw e;
                }
            }
        }
        
        return null; // Should not reach here
    }

    /**
     * Enum to track reconciliation results
     */
    private enum ReconciliationResult {
        RECONCILED,
        SKIPPED,
        ERROR
    }
}

