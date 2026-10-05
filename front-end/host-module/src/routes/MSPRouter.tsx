import { Navigate, Route, Routes } from 'react-router-dom';

import MSPPaymentHistory from 'pages/billing/MSPPaymentHistory';
import MSPPendingPayment from 'pages/billing/MSPPendingPayment';
import MSPActivityLogs from 'pages/msp/ActivityLogs';
import MSPAnalytics from 'pages/msp/Analytics';
import MSPEdit from 'pages/msp/Edit';
import MSPList from 'pages/msp/List';
import MSPManageLicenses from 'pages/msp/ManageLicenses';
import MSPOnboarding from 'pages/msp/Onboarding';
import MSPProfile from 'pages/msp/Profile';
import MSPReports from 'pages/msp/Reports';
import MSPSuspend from 'pages/msp/Suspend';
import { routes } from 'routes/AppRoutes';

const MSPRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="msp-onboarding" />} />

      <Route path={routes.mspOnboarding.path} element={<MSPOnboarding />} />
      <Route path={routes.mspList.path} element={<MSPList />} />
      <Route path={routes.mspEdit.path} element={<MSPEdit />} />
      <Route path={routes.mspProfile.path} element={<MSPProfile />} />
      <Route
        path={routes.mspPaymentHistory.path}
        element={<MSPPaymentHistory />}
      />
      <Route
        path={routes.mspPendingPayments.path}
        element={<MSPPendingPayment />}
      />
      <Route
        path={routes.mspManageLicenses.path}
        element={<MSPManageLicenses />}
      />
      <Route path={routes.mspSuspend.path} element={<MSPSuspend />} />
      <Route path={routes.mspActivityLogs.path} element={<MSPActivityLogs />} />
      <Route path={routes.mspReports.path} element={<MSPReports />} />
      <Route path={routes.mspAnalytics.path} element={<MSPAnalytics />} />
    </Routes>
  );
};

export default MSPRouter;
