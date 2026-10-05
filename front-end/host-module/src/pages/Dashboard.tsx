import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { CertificateManagementRoutes } from 'routes/CertificateManagementRoutes';
import { ContentManagementRoutes } from 'routes/ContentManagementRoutes';
import { LicenseManagementRoutes } from 'routes/LicenseManagementRoutes';
import { PackageManagementRoutes } from 'routes/PackageManagementRoutes';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';

const RemoteDashboard = lazy(() => import('home-module/Dashboard'));

const Dashboard = () => {
  const moduleRoutes = {
    ...ContentManagementRoutes,
    ...PackageManagementRoutes,
    ...LicenseManagementRoutes,
    ...UserManagementRoutes,
    ...CertificateManagementRoutes,
  };

  return (
    <ErrorBoundaryWrapper>
      <RemoteDashboard hostPath={moduleRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default Dashboard;
