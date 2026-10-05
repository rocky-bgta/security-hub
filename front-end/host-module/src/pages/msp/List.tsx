import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { MSPManagementRoutes } from 'routes/MSPManagementRoutes';

const RemoteMSPList = lazy(() => import('user-module/MSPList'));

const MSPList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPList hostPath={MSPManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default MSPList;
