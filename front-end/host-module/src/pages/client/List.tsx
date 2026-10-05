import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientManagementRoutes } from 'routes/ClientManagementRoutes';

const RemoteClientList = lazy(() => import('user-module/ClientList'));

const ClientList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientList hostPath={ClientManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ClientList;
