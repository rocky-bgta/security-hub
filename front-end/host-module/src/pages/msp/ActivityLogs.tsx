import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPActivityLogs = lazy(() => import('user-module/MSPActivityLogs'));

const MSPActivityLogs = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPActivityLogs />
    </ErrorBoundaryWrapper>
  );
};

export default MSPActivityLogs;
