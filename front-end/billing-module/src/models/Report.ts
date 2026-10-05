export interface IAnalyticsSummary {
  totalRevenue: number;
  totalRevenueChangePercent: number;
  totalRefunds: number;
  refundRate: number;
  failedPaymentsCount: number;
  failedPaymentsLabel: string;
  newSubscriptions: number;
  newSubscriptionsChangePercent: number;
  activeLicenses: number;
  activeLicensesLabel: string;
}

export interface IRevenueTrend {
  period: string;
  revenue: number;
  newSubscriptions: number;
  refunds: number;
  netRevenue: number;
}

export interface ITopPerformingPackage {
  packageName: string;
  subscriptionCount: number;
  revenue: number;
  revenuePercentage: number;
}

export interface IFailedPayment {
  reason: string;
  reasonLabel: string;
  count: number;
  percentage: number;
}

export interface IPaymentSuccessRate {
  paymentSuccessRate: number;
  totalPayments: number;
  successfulPayments: number;
  failedPayments: number;
}
