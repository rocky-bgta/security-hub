import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPAnalytics = lazy(() => import('user-module/MSPAnalytics'));

const MSPAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default MSPAnalytics;
