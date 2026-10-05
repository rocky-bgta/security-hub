import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { MSPManagementRoutes } from 'routes/MSPManagementRoutes';

const RemoteMSPEdit = lazy(() => import('user-module/MSPEdit'));

const MSPEdit = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPEdit hostPath={MSPManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default MSPEdit;
