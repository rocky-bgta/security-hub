import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { MSPManagementRoutes } from 'routes/MSPManagementRoutes';

const RemoteMSPProfile = lazy(() => import('user-module/MSPProfile'));

const MSPProfile = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPProfile hostPath={MSPManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default MSPProfile;
