import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPReports = lazy(() => import('user-module/MSPReports'));

const MSPReports = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPReports />
    </ErrorBoundaryWrapper>
  );
};

export default MSPReports;
