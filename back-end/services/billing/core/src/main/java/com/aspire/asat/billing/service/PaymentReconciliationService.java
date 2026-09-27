package com.aspire.asat.billing.service;

/**
 * Service for reconciling payments with payment gateway APIs
 * to identify and process payments that succeeded but weren't processed locally
 */
public interface PaymentReconciliationService {

    /**
     * Reconciles Stripe payments by checking PaymentIntent status
     * for PENDING online payments and updating them if they succeeded in Stripe
     */
    void reconcileStripePayments();
}

