import { Navigate, Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layouts/BaseLayout';
import RoleBasedProtectedRoute from 'components/layouts/RoleBasedProtectedRoute';
import SelfClientAdminLayout from 'components/layouts/SelfClientAdminLayout';
import ActivityLogs from 'pages/ActivityLogs';
import AllUsers from 'pages/AllUsers';
import Analytics from 'pages/aspire-admin/Analytics';
import ClientUsersList from 'pages/aspire-admin/ClientUsersList';
import Onboarding from 'pages/aspire-admin/Onboarding';
import Reports from 'pages/aspire-admin/Reports';
import SuspendUser from 'pages/aspire-admin/SuspendUser';
import SyncUsers from 'pages/aspire-admin/SyncUsers';
import BulkImport from 'pages/BulkImport';
import ClientAnalytics from 'pages/client-admin/Analytics';
import ClientSuspend from 'pages/client-admin/ClientSuspend';
import ClientEdit from 'pages/client-admin/Edit';
import ClientLicense from 'pages/client-admin/License';
import ClientLicenseAllocation from 'pages/client-admin/LicenseAllocation';
import ClientList from 'pages/client-admin/List';
import ClientProfile from 'pages/client-admin/Profile';
import ClientReports from 'pages/client-admin/Reports';
import SelfOnBoarding from 'pages/client-admin/SelfOnBoarding';
import Login from 'pages/Login';
import MFASetup from 'pages/MFASetup';
import MFAVerify from 'pages/MFAVerify';
import MSPActivityLogs from 'pages/msp-admin/ActivityLogs';
import MSPAnalytics from 'pages/msp-admin/Analytics';
import MSPEdit from 'pages/msp-admin/Edit';
import MSPLicense from 'pages/msp-admin/License';
import MSPLicenseAllocation from 'pages/msp-admin/LicenseAllocation';
import MSPList from 'pages/msp-admin/List';
import MSPManageLicenses from 'pages/msp-admin/ManageLicenses';
import MSPOnBoarding from 'pages/msp-admin/OnBoarding';
import MSPProfile from 'pages/msp-admin/Profile';
import MSPReports from 'pages/msp-admin/Reports';
import MSPSuspend from 'pages/msp-admin/Suspend';
import NotFound from 'pages/NotFound';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import BuyProduct from 'pages/client-admin/BuyProduct';
import ClientUserActivityLogs from 'pages/client-admin/ClientUserActivityLogs';
import ClientActivityLogs from 'pages/client-admin/ClientActivityLogs';
import OnBoarding from 'pages/OnBoarding';
import SystemUsers from 'pages/SystemUsers';

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route index element={<Navigate to={routes.users.path} replace />} />

        <Route
          path={routes.userOnboarding.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}
            >
              <Onboarding />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.userBulkImport.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.ASPIRE_ADMIN, ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN]}
            >
              <BulkImport />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.syncUser.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.ASPIRE_ADMIN, ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN]}
            >
              <SyncUsers />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.userReport.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <Reports />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.userAnalytics.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <Analytics />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.suspendUser.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <SuspendUser />
            </RoleBasedProtectedRoute>
          }
        />

        <Route path={routes.users.path} element={<AllUsers />} />
        <Route path={routes.mspOnboarding.path} element={<MSPOnBoarding />} />
        <Route path={routes.mspList.path} element={<MSPList />} />
        <Route
          path={routes.mspEdit.path}
          element={<MSPEdit hostPath={routes} />}
        />
        <Route
          path={routes.mspProfile.path}
          element={<MSPProfile hostPath={routes} />}
        />
        <Route
          path={routes.mspLicense.path}
          element={<MSPLicense hostPath={routes} />}
        />
        <Route
          path={routes.mspManageLicenses.path}
          element={<MSPManageLicenses />}
        />
        <Route
          path={routes.mspLicenseAllocation.path}
          element={<MSPLicenseAllocation />}
        />
        <Route
          path={routes.mspSuspend.path}
          element={<MSPSuspend hostPath={routes} />}
        />
        <Route path={routes.mspReports.path} element={<MSPReports />} />
        <Route path={routes.mspAnalytics.path} element={<MSPAnalytics />} />

        <Route
          path={routes.clientUserList.path}
          element={<ClientUsersList />}
        />

        <Route path={routes.clientOnboarding.path} element={<OnBoarding />} />
        <Route
          path={routes.clientList.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientList hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientEdit.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientEdit hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientProfile.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientProfile hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientLicense.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientLicense hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientLicenseAllocation.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientLicenseAllocation />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientSuspend.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientSuspend hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.mspActivityLogs.path}
          element={<MSPActivityLogs />}
        />
        <Route
          path={routes.clientActivityLogs.path}
          element={<ClientActivityLogs />}
        />
        <Route
          path={routes.activityLogs.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN, ROLE.CLIENT_USER, ROLE.MSP_ADMIN]}
            >
              <ActivityLogs />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.userActivityLogs.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_ADMIN]}>
              <ClientUserActivityLogs />
            </RoleBasedProtectedRoute>
          }
        />
        <Route path={routes.clientReports.path} element={<ClientReports />} />
        <Route
          path={routes.clientAnalytics.path}
          element={<ClientAnalytics />}
        />

        <Route path={routes.buyProduct.path} element={<BuyProduct />} />

        <Route
          path={routes.systemUsers.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <SystemUsers />
            </RoleBasedProtectedRoute>
          }
        />
      </Route>

      <Route path={routes.login.path} element={<Login />} />
      <Route path={routes.twoFactorAuthSetup.path} element={<MFASetup />} />
      <Route path={routes.twoFactorAuthVerify.path} element={<MFAVerify />} />

      <Route
        path={routes.selfOnboard.path}
        element={
          <SelfClientAdminLayout>
            <SelfOnBoarding />
          </SelfClientAdminLayout>
        }
      />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
