import { Route, Routes } from 'react-router-dom';

import BillingManagement from 'pages/billing';
import BillingAnalytics from 'pages/billing/BillingAnalytics';
import BillingHistory from 'pages/billing/BillingHistory';
import ClientBillingReport from 'pages/billing/ClientBillingReport';
import ClientManageLicense from 'pages/billing/ClientManageLicenses';
import PaymentHistory from 'pages/billing/PaymentHistory';
import ClientPendingPayment from 'pages/billing/ClientPendingPayment';
import CreditDetails from 'pages/billing/CreditDetails';
import CreditUseHistory from 'pages/billing/CreditUseHistory';
import InvoiceHistory from 'pages/billing/InvoiceHistory';
import PaymentFailed from 'pages/billing/PaymentFailed';
import PaymentReport from 'pages/billing/PaymentReport';
import PaymentSuccess from 'pages/billing/PaymentSuccess';
import PendingPayment from 'pages/billing/PendingPayment';
import { routes } from 'routes/AppRoutes';

const BillingRouter = () => {
  return (
    <Routes>
      <Route index element={<BillingManagement />} />

      <Route path={routes.billingHistory.path} element={<BillingHistory />} />
      <Route path={routes.paymentHistory.path} element={<PaymentHistory />} />
      <Route path={routes.invoiceHistory.path} element={<InvoiceHistory />} />
      <Route path={routes.pendingPayment.path} element={<PendingPayment />} />
      <Route path={routes.paymentReport.path} element={<PaymentReport />} />
      <Route
        path={routes.billingAnalytics.path}
        element={<BillingAnalytics />}
      />

      <Route path={routes.paymentSuccess.path} element={<PaymentSuccess />} />
      <Route path={routes.paymentFailed.path} element={<PaymentFailed />} />

      <Route
        path={routes.clientBillingReport.path}
        element={<ClientBillingReport />}
      />

      <Route
        path={routes.clientPendingPayments.path}
        element={<ClientPendingPayment />}
      />
      {/* <Route
        path={routes.clientPaymentHistory.path}
        element={<ClientPaymentHistory />}
      /> */}
      <Route
        path={routes.clientManageLicenses.path}
        element={<ClientManageLicense />}
      />

      <Route path={routes.creditDetails.path} element={<CreditDetails />} />
      <Route
        path={routes.creditUseHistory.path}
        element={<CreditUseHistory />}
      />
    </Routes>
  );
};

export default BillingRouter;
