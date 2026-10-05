import { Navigate, Outlet, useLocation } from 'react-router-dom';

import AspireAdminLayout from 'components/layout/AspireAdminLayout';
import ClientAdminLayout from 'components/layout/ClientAdminLayout';
import ClientUserLayout from 'components/layout/ClientUserLayout';
import SuperAdminLayout from 'components/layout/SuperAdminLayout';
import Loader from 'components/Loader';
import { useAuth } from 'hooks/UseAuth';
import { useStore } from 'hooks/UseStore';
import { AccountManagementRoutes } from 'routes/AccountManagementRoutes';
import { routes } from 'routes/AppRoutes';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';
import { CertificateManagementRoutes } from 'routes/CertificateManagementRoutes';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';
import { KnowledgeHubManagementRoutes } from 'routes/KnowledgeHubManagementRoutes';
import { LeaderboardManagementRoutes } from 'routes/LeaderboardManagementRoutes';
import { LicenseManagementRoutes } from 'routes/LicenseManagementRoutes';
import { PackageManagementRoutes } from 'routes/PackageManagementRoutes';
import { PolicyManagementRoutes } from 'routes/PolicyManagementRoutes';
import { ProductManagementRoutes } from 'routes/ProductManagementRoutes';
import { ReportManagementRoutes } from 'routes/ReportManagementRoutes';
import { SupportManagementRoutes } from 'routes/SupportManagementRoutes';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';
import { MSPManagementRoutes } from 'routes/MSPManagementRoutes';
import { ROLE } from 'utils/Role';
import MspLayout from './MspLayout';

const BaseLayout = () => {
  const { loading, role, isAuthenticated } = useAuth();
  const { userInfo, isStoreReady } = useStore();
  const location = useLocation();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return (
      <Navigate to={routes.login.path} replace state={{ from: location }} />
    );
  }

  // Wait for store init / user-details only — not menus (slow remote Mongo).
  if (!isStoreReady || !userInfo.userId) {
    return <Loader />;
  }

  if (userInfo.selfOnboardingUser) {
    return <Navigate to={routes.selfOnboard.path} replace />;
  }

  if (role === ROLE.SUPER_ADMIN) {
    return (
      <SuperAdminLayout
        hostPath={{ ...UserManagementRoutes, ...ContentManagementRoutes }}
      >
        <Outlet />
      </SuperAdminLayout>
    );
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.FINANCE_ADMIN) {
    return (
      <AspireAdminLayout
        hostPath={{
          ...AspireAdminRoutes,
        }}
      >
        <Outlet />
      </AspireAdminLayout>
    );
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return (
      <ClientAdminLayout
        hostPath={{
          ...UserManagementRoutes,
          ...BillingManagementRoutes,
          ...ProductManagementRoutes,
          ...PackageManagementRoutes,
          ...LicenseManagementRoutes,
          ...SupportManagementRoutes,
          ...ReportManagementRoutes,
          ...PolicyManagementRoutes,
          ...CertificateManagementRoutes,
          ...KnowledgeHubManagementRoutes,
          ...LeaderboardManagementRoutes,
          ...AccountManagementRoutes,
        }}
      >
        <Outlet />
      </ClientAdminLayout>
    );
  }

  if (role === ROLE.MSP_ADMIN) {
    return (
      <MspLayout hostPath={{ ...UserManagementRoutes, ...BillingManagementRoutes, ...MSPManagementRoutes }}>
        <Outlet />
      </MspLayout>
    );
  }

  if (role === ROLE.CLIENT_USER) {
    return (
      <ClientUserLayout hostPath={{ ...ClientUserRoutes }}>
        <Outlet />
      </ClientUserLayout>
    );
  }

  return <Loader />;
};

export default BaseLayout;
