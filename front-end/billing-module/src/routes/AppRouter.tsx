import { Navigate, Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layouts/BaseLayout';
import CouponList from 'pages/CouponList';
import CreditDetails from 'pages/credit/CreditDetails';
import CreditList from 'pages/credit/CreditList';
import CreditUseHistory from 'pages/credit/CreditUseHistory';
import AspireBillingsAnalytics from 'pages/payment/AspireBillingAnalytics';
import AspireBillingHistory from 'pages/payment/AspireBillingHistory';
import ClientBillingReport from 'pages/payment/ClientBillingReport';
import ClientManageLicense from 'pages/payment/ClientManageLicense';
import ClientPaymentHistory from 'pages/payment/ClientPaymentHistory';
import ClientPendingPayment from 'pages/payment/ClientPendingPayment';
import MSPManageLicense from 'pages/payment/MSPManageLicense';
import MSPPaymentReport from 'pages/payment/MSPPaymentReport';
import MSPPaymentReportDetails from 'pages/payment/MSPPaymentReportDetails';
import MSPPendingPayment from 'pages/payment/MSPPendingPayment';
import MSPPaymentHistory from 'pages/payment/MSPPaymentHistory';
import PaymentFailed from 'pages/payment/PaymentFailed';
import PaymentSuccess from 'pages/payment/PaymentSuccess';
import PendingPayment from 'pages/payment/PendingPayment';
import VatList from 'pages/vat/VatList';
import { routes } from 'routes/Routes';
import Login from 'pages/Login';
import PaymentHistory from 'pages/payment/PaymentHistory';
import MFASetup from 'pages/MFASetup';
import MFAVerify from 'pages/MFAVerify';
import TierList from 'pages/settings/TierList';

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route
          index
          element={<Navigate to={routes.pendingPayment.path} replace />}
        />

        <Route path={routes.pendingPayment.path} element={<PendingPayment />} />
        <Route path={routes.paymentHistory.path} element={<PaymentHistory />} />
        <Route path={routes.paymentSuccess.path} element={<PaymentSuccess />} />
        <Route path={routes.paymentFailed.path} element={<PaymentFailed />} />

        <Route
          path={routes.clientBillingReport.path}
          element={<ClientBillingReport />}
        />
        <Route
          path={routes.mspPendingPayments.path}
          element={<MSPPendingPayment />}
        />
        <Route
          path={routes.mspPaymentReport.path}
          element={<MSPPaymentReport />}
        />
        <Route
          path={routes.mspPaymentReportDetails.path}
          element={<MSPPaymentReportDetails />}
        />
        <Route
          path={routes.mspManageLicenses.path}
          element={<MSPManageLicense />}
        />
        <Route
          path={routes.mspPaymentHistory.path}
          element={<MSPPaymentHistory />}
        />

        <Route
          path={routes.clientPendingPayments.path}
          element={<ClientPendingPayment />}
        />
        <Route
          path={routes.clientPaymentHistory.path}
          element={<ClientPaymentHistory />}
        />
        <Route
          path={routes.clientManageLicenses.path}
          element={<ClientManageLicense />}
        />

        <Route
          path={routes.billingHistory.path}
          element={<AspireBillingHistory />}
        />
        <Route
          path={routes.billingAnalytics.path}
          element={<AspireBillingsAnalytics />}
        />

        <Route path={routes.manageCoupon.path} element={<CouponList />} />
        <Route path={routes.manageCredits.path} element={<CreditList />} />
        <Route path={routes.creditDetails.path} element={<CreditDetails />} />
        <Route
          path={routes.creditUseHistory.path}
          element={<CreditUseHistory />}
        />
        <Route path={routes.vatConfigurations.path} element={<VatList />} />
        <Route path={routes.tierConfigurations.path} element={<TierList />} />
      </Route>
      <Route path={routes.twoFactorAuthSetup.path} element={<MFASetup />} />
      <Route path={routes.twoFactorAuthVerify.path} element={<MFAVerify />} />
      <Route path={routes.login.path} element={<Login />} />
    </Routes>
  );
};

export default AppRouter;
