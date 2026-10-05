import { Navigate, Route, Routes } from 'react-router-dom';

import ClientManageLicense from 'pages/billing/ClientManageLicenses';
import ClientPaymentHistory from 'pages/billing/ClientPaymentHistory';
import ClientPendingPayment from 'pages/billing/ClientPendingPayment';
import ClientActivityLogs from 'pages/client/ActivityLogs';
import ClientAnalytics from 'pages/client/Analytics';
import ClientList from 'pages/client/List';
import ClientOnboarding from 'pages/client/Onboarding';
import ClientReports from 'pages/client/Reports';
import ClientSuspend from 'pages/client/Suspend';
import { routes } from 'routes/AppRoutes';
import ClientUserList from 'pages/client/ClientUserList';
import ClientEdit from 'pages/client/Edit';

const ClientRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="client-onboarding" />} />

      <Route
        path={routes.clientOnboarding.path}
        element={<ClientOnboarding />}
      />
      <Route path={routes.clientEdit.path} element={<ClientEdit />} />
      <Route path={routes.clientList.path} element={<ClientList />} />
      <Route path={routes.clientUserList.path} element={<ClientUserList />} />
      <Route
        path={routes.clientPaymentHistory.path}
        element={<ClientPaymentHistory />}
      />
      <Route
        path={routes.clientPendingPayments.path}
        element={<ClientPendingPayment />}
      />
      <Route
        path={routes.clientManageLicenses.path}
        element={<ClientManageLicense />}
      />
      <Route path={routes.clientSuspend.path} element={<ClientSuspend />} />
      <Route
        path={routes.clientActivityLogs.path}
        element={<ClientActivityLogs />}
      />
      <Route path={routes.clientReports.path} element={<ClientReports />} />
      <Route path={routes.clientAnalytics.path} element={<ClientAnalytics />} />
    </Routes>
  );
};

export default ClientRouter;
