import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import DuePaymentReport from 'features/report/billing-payment-reports/DuePaymentReport';
import InvoiceSummaryReport from 'features/report/billing-payment-reports/InvoiceSummaryReport';
import PaymentReport from 'features/report/billing-payment-reports/PaymentReport';
import SubscriptionSummaryReport from 'features/report/billing-payment-reports/SubscriptionSummaryReport';

import { useState } from 'react';

type ReportTabValue =
  | 'subscription-summary'
  | 'payment'
  | 'due-payment'
  | 'invoice-summary';

const BillingPaymentReport = () => {
  const [activeTab, setActiveTab] = useState<ReportTabValue>(
    'subscription-summary',
  );

  const tabSubHeaders: Record<ReportTabValue, string> = {
    'subscription-summary':
      'Overview of all subscriptions, status, expiry, and usage',
    payment: 'Payment history, revenue totals, and transaction outcomes',
    'due-payment': 'Outstanding dues, overdue balances, and upcoming payments',
    'invoice-summary':
      'Invoice status breakdown across paid, unpaid, and disputed',
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          Billing and Payment Report
        </h1>
        <p className="text-muted-foreground">{tabSubHeaders[activeTab]}</p>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={value => setActiveTab(value as ReportTabValue)}
        className="space-y-6"
      >
        <TabsList>
          <TabsTrigger value="subscription-summary">
            Subscription Summary
          </TabsTrigger>
          <TabsTrigger value="payment">Payment</TabsTrigger>
          <TabsTrigger value="due-payment">Due Payment</TabsTrigger>
          <TabsTrigger value="invoice-summary">Invoice Summary</TabsTrigger>
        </TabsList>

        <TabsContent value="subscription-summary">
          <SubscriptionSummaryReport />
        </TabsContent>

        <TabsContent value="payment">
          <PaymentReport />
        </TabsContent>

        <TabsContent value="due-payment">
          <DuePaymentReport />
        </TabsContent>

        <TabsContent value="invoice-summary">
          <InvoiceSummaryReport />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default BillingPaymentReport;
