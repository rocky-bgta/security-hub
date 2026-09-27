package com.aspire.asat.billing.utils;

import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.repo.PaymentRepository;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.common.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sending payment-related notifications to billing email addresses
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationUtil {

    private final NotificationClient notificationClient;
    private final PaymentRepository paymentRepository;

    /**
     * Send payment success notification to billing email
     *
     * @param payment The payment that succeeded
     * @param invoice The invoice associated with the payment
     */
    public void sendPaymentSuccessNotification(Payment payment, Invoice invoice) {
        try {
            // Validate billing email
            if (invoice.getBillingEmail() == null || invoice.getBillingEmail().trim().isEmpty()) {
                log.warn("Cannot send payment success notification: billingEmail is null or empty for invoice: {}", invoice.getId());
                return;
            }

            // Calculate payment details
            double totalPaid = calculateTotalPaidAmount(invoice.getId());
            double outstandingAmount = Math.max(0, invoice.getTotalAmount() - totalPaid);
            String paymentMethod = resolvePaymentMethod(payment);

            // Format timestamp
            Instant timestampInstant = payment.getPaymentDate() != null 
                ? payment.getPaymentDate() 
                : (payment.getCreatedAt() != null ? payment.getCreatedAt() : Instant.now());
            String timestamp = timestampInstant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Prepare template model
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("paymentId", payment.getId());
            templateModel.put("invoiceId", invoice.getId());
            templateModel.put("amount", payment.getAmount() != null ? payment.getAmount() : 0.0);
            templateModel.put("currency", payment.getCurrency() != null ? payment.getCurrency() : "USD");
            templateModel.put("paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "");
            templateModel.put("transactionId", payment.getTransactionId());
            templateModel.put("paymentMethod", paymentMethod);
            templateModel.put("invoiceTotal", invoice.getTotalAmount());
            templateModel.put("totalPaid", totalPaid);
            templateModel.put("outstandingAmount", outstandingAmount);
            templateModel.put("clientName", invoice.getClientName() != null ? invoice.getClientName() : "Client");
            
            // Map to template-expected variable names
            templateModel.put("method", paymentMethod);
            templateModel.put("timestamp", timestamp);
            templateModel.put("userName", invoice.getClientName() != null ? invoice.getClientName() : "Client");
            templateModel.put("adminName", invoice.getClientName() != null ? invoice.getClientName() : "Admin");

            // Send email notification
            boolean sent = notificationClient.sendEmailNotification(
                    invoice.getBillingEmail(),
                    NotificationType.PAYMENT_SUCCESS,
                    templateModel
            );

            if (sent) {
                log.info("Payment success notification sent to billingEmail: {} for payment: {}", 
                        invoice.getBillingEmail(), payment.getId());
            } else {
                log.warn("Failed to send payment success notification to billingEmail: {} for payment: {}", 
                        invoice.getBillingEmail(), payment.getId());
            }
        } catch (Exception e) {
            log.error("Error sending payment success notification for payment: {}: {}", 
                    payment.getId(), e.getMessage(), e);
            // Don't throw - notification failure shouldn't break payment processing
        }
    }

    /**
     * Send payment failure notification to billing email
     *
     * @param payment The payment that failed
     * @param invoice The invoice associated with the payment
     */
    public void sendPaymentFailureNotification(Payment payment, Invoice invoice) {
        try {
            // Validate billing email
            if (invoice.getBillingEmail() == null || invoice.getBillingEmail().trim().isEmpty()) {
                log.warn("Cannot send payment failure notification: billingEmail is null or empty for invoice: {}", invoice.getId());
                return;
            }

            // Prepare template model
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("paymentId", payment.getId());
            templateModel.put("invoiceId", invoice.getId());
            templateModel.put("amount", payment.getAmount() != null ? payment.getAmount() : 0.0);
            templateModel.put("currency", payment.getCurrency() != null ? payment.getCurrency() : "USD");
            templateModel.put("paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "");
            templateModel.put("transactionId", payment.getTransactionId());
            templateModel.put("failureReason", "Payment processing failed");
            templateModel.put("invoiceTotal", invoice.getTotalAmount());
            templateModel.put("clientName", invoice.getClientName() != null ? invoice.getClientName() : "Client");

            // Send email notification
            boolean sent = notificationClient.sendEmailNotification(
                    invoice.getBillingEmail(),
                    NotificationType.PAYMENT_FAILURE,
                    templateModel
            );

            if (sent) {
                log.info("Payment failure notification sent to billingEmail: {} for payment: {}", 
                        invoice.getBillingEmail(), payment.getId());
            } else {
                log.warn("Failed to send payment failure notification to billingEmail: {} for payment: {}", 
                        invoice.getBillingEmail(), payment.getId());
            }
        } catch (Exception e) {
            log.error("Error sending payment failure notification for payment: {}: {}", 
                    payment.getId(), e.getMessage(), e);
            // Don't throw - notification failure shouldn't break payment processing
        }
    }

    /**
     * Calculates the total amount paid for an invoice by summing all successful payments.
     * 
     * @param invoiceId The invoice ID
     * @return Total paid amount (0.0 if no successful payments)
     */
    private double calculateTotalPaidAmount(String invoiceId) {
        return MoneyUtil.round(paymentRepository.findByInvoiceId(invoiceId).stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getAmount() != null)
                .mapToDouble(Payment::getAmount)
                .sum());
    }

    /**
     * Resolves the payment method from payment sources
     *
     * @param payment The payment object
     * @return Payment method string or "Unknown" if not available
     */
    private String resolvePaymentMethod(Payment payment) {
        if (payment.getPaymentSources() == null || payment.getPaymentSources().isEmpty()) {
            return "Unknown";
        }
        return payment.getPaymentSources().get(0).getMethod(); // assuming first method is primary
    }
}

