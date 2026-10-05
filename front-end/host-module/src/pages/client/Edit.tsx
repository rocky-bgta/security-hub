import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientManagementRoutes } from 'routes/ClientManagementRoutes';

const RemoteClientEdit = lazy(() => import('user-module/ClientEdit'));

const ClientEdit = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientEdit hostPath={ClientManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default ClientEdit;
