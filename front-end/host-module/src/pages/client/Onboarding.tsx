import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientManagementRoutes } from 'routes/ClientManagementRoutes';

const RemoteClientOnboarding = lazy(
  () => import('user-module/OnBoarding'),
);

const ClientOnboarding = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientOnboarding hostPath={ClientManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ClientOnboarding;
