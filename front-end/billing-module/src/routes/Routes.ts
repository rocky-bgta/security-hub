export const routes = {
  login: {
    title: 'Login',
    key: 'login',
    path: '/auth/login',
  },
  twoFactorAuthSetup: {
    title: 'Two Factor Authentication Setup',
    key: 'two-factor-auth-setup',
    path: '/auth/two-factor-authentication/setup',
  },
  twoFactorAuthVerify: {
    title: 'Two Factor Authentication Verify',
    key: 'two-factor-auth-verify',
    path: '/auth/two-factor-authentication/verify',
  },
  pendingPayment: {
    title: 'Pending Payment',
    key: 'pending-payment',
    path: '/billing-management/pending-payment',
  },
  paymentHistory: {
    title: 'Payment History',
    key: 'payment-history',
    path: '/billing-management/payment-history',
  },
  paymentSuccess: {
    title: 'Payment Success',
    key: 'payment-success',
    path: '/payment-success',
  },
  paymentFailed: {
    title: 'Payment Failed',
    key: 'payment-failed',
    path: '/payment-failed',
  },

  clientBillingReport: {
    title: 'Client Billing Report',
    key: 'client-billing-report',
    path: '/client-billing-report',
  },

  mspPendingPayments: {
    title: 'MSP Pending Payments',
    key: 'msp-pending-payment',
    path: '/msp-management/msp-pending-payments',
  },
  mspPaymentReport: {
    title: 'MSP Payment Reports',
    key: 'msp-payment-report',
    path: '/msp-management/msp-payment-report',
  },
  mspPaymentReportDetails: {
    title: 'MSP Payment Report Details',
    key: 'msp-payment-report-details',
    path: '/msp-payment-report/:id',
  },
  mspManageLicenses: {
    title: 'MSP Manage Licenses',
    key: 'msp-manage-licenses',
    path: '/msp-management/msp-manage-licenses',
  },
  mspPaymentHistory: {
    title: 'MSP Payment History',
    key: 'msp-payment-history',
    path: '/msp-management/msp-payment-history',
  },

  clientPendingPayments: {
    title: 'Client Pending List',
    key: 'client-pending-list',
    path: '/client-management/client-pending-list',
  },
  clientPaymentHistory: {
    title: 'Client Payment History',
    key: 'client-payment-history',
    path: '/client-management/client-payment-history',
  },
  clientManageLicenses: {
    title: 'Client Manage Licenses',
    key: 'client-manage-licenses',
    path: '/client-management/client-manage-licenses',
  },

  billingHistory: {
    title: 'Billing History',
    key: 'billing-history',
    path: '/billing-management/billing-history',
  },
  billingAnalytics: {
    title: 'Billing Analytics',
    key: 'billing-analytics',
    path: '/billing-management/billing-analytics',
  },

  manageCredits: {
    title: 'Manage Credits',
    key: 'manage-credits',
    path: '/settings/manage-credits',
  },
  creditDetails: {
    title: 'Credit Details',
    key: 'credit-details',
    path: '/credit-details/:id',
  },
  creditUseHistory: {
    title: 'Credit Use History',
    key: 'credit-use-history',
    path: '/credit-use-history/:id',
  },
  vatConfigurations: {
    title: 'VAT Configurations',
    key: 'vat-configurations',
    path: '/settings/vat-configurations',
  },
  tierConfigurations: {
    title: 'Tier Configurations',
    key: 'tier-configurations',
    path: '/settings/tier-configurations',
  },
  manageCoupon: {
    title: 'Manage Coupons',
    key: 'manage-coupon',
    path: '/settings/manage-coupon',
  },
};
