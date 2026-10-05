import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPSuspend = lazy(() => import('user-module/MSPSuspend'));

const MSPSuspend = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPSuspend />
    </ErrorBoundaryWrapper>
  );
};

export default MSPSuspend;
